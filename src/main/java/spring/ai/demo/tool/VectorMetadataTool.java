package spring.ai.demo.tool;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VectorMetadataTool {
    private static final Logger logger = LoggerFactory.getLogger(VectorMetadataTool.class);

    private final VectorStore vectorStore;

    @Tool(description = "Get the metadata information (e.g., owner, apid, platform...) of a service based on its repository name.")
    public String getOwnerByRepoName(
            @ToolParam(description = "name of the service's repository") String repoName,
            @ToolParam(description = "the key of the metadata") String key) {
        logger.info("Use tools to get repo information: {}", repoName);
        List<Document> docs = vectorStore.similaritySearch(repoName);
        if (docs != null && !docs.isEmpty()) {
            Object info = docs.get(0).getMetadata().get(key);
            if (info != null) {
                return info.toString();
            }
        }
        return "Unknown";
    }

    @Tool(description = "更新向量資料中 指定repo_name的指定資料欄位")
    public String updateVectorData(
            @ToolParam(description = "name of the service's repository") String repoName,
            @ToolParam(description = "the key of the metadata") String key,
            @ToolParam(description = "the value of the metadata") String value
    ) {
        logger.info("Use tools to update metadata for repo: {}", repoName);

        List<Document> docs = vectorStore.similaritySearch(repoName);
        if (docs != null && !docs.isEmpty()) {
            Document oldDoc = docs.get(0);
            String id = oldDoc.getId();
            Map<String, Object> newMetadata = new HashMap<>(oldDoc.getMetadata());
            newMetadata.remove("distance");
            newMetadata.put(key, value);

            vectorStore.delete(List.of(id));
            Document newDoc = new Document(oldDoc.getText(), newMetadata);
            vectorStore.add(List.of(newDoc));
            return "Update Successfully.";

        }
        return "Unknown";

    }
}