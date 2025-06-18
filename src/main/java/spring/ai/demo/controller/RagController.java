package spring.ai.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.demo.dto.TypeResponse;
import spring.ai.demo.dto.VectorMetadataDto;
import spring.ai.demo.entity.ServicesInfoEntity;
import spring.ai.demo.repository.ServicesInfoRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RagController {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    @GetMapping("/classify")
    public TypeResponse classify(@RequestParam String text) {

        return chatClient.prompt()
                .system("Classify the type of the provided text.")
                .user(text)
                .call()
//                .content();
                .entity(TypeResponse.class);
    }

    @GetMapping("similar")
    List<Document> similarSearch(String query) {
        //pg vector api
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(4)
                .similarityThreshold(0.8)
                .build());
    }

    @GetMapping("/rag")
    public String classifyWithRetrieval(@RequestParam String text) {

//        RetrievalAugmentationAdvisor顧問 實際上是做一次 similaritySearch(text)
//        拿到documents後 自動補到prompt裡面
//        允許未找到相關文件時恢復到一般回應
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(TranslationQueryTransformer.builder()
                        .chatClientBuilder(chatClient.mutate())
                        .targetLanguage("english")
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.8)
                        .topK(10)
                        .vectorStore(vectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();
        return chatClient.prompt()
//                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .advisors(retrievalAugmentationAdvisor)
                .user(text)
                .call()
                .content();
    }

    @PostMapping(value="/uploadfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        InputStreamResource resource = new InputStreamResource(file.getInputStream()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        // 使用 TextDocumentReader 讀取檔案內容
        TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
        List<Document> fileDocuments = tikaReader.get();
        // 基於Token將多組Document進行更細化的分割
        List<Document> documents = tokenTextSplitter.apply(fileDocuments);
        vectorStore.accept(documents);

        return "File uploaded successfully! Documents added: " + documents.size();
    }

    @PostMapping("/add")
    public String addDocument(@RequestParam String text, @RequestBody VectorMetadataDto metadataDto) {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper
                .convertValue(metadataDto, new TypeReference<>() {
                });
        Document doc = new Document(text, map);
        vectorStore.add(List.of(doc));
        return "Document added!";
    }


    @GetMapping("/embed")
    public float[] embed(String name) {
        // embedding Model
        return embeddingModel.embed(name);
    }


    /*
    Demo: SQL LIKE Search
     */


    private final ServicesInfoRepository servicesInfoRepository;

    @PostMapping("/services")
    public List<String> getServices() {
        return servicesInfoRepository.findNameByIsActive("Y");
    }

    @GetMapping("/sqlsearch")
    public List<ServicesInfoEntity> sqlLikeName(@RequestParam String name) {
        return servicesInfoRepository.findByNameContaining(name);
    }

}
