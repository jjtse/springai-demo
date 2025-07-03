package spring.ai.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AiController {

    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;


    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/doc")
    public String docPage() {
        return "doc";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @GetMapping("/prompt")
    public String chat(@RequestParam String text, Model model) {
        PromptTemplate promptTemplate = new PromptTemplate("請向我介紹{text}是什麼");

        Prompt prompt = promptTemplate.create(Map.of("text", text));
        ChatResponse response = chatModel.call(prompt);
//        chatClient.prompt()
//                .user(u -> u.text("請向我介紹{text}是什麼")
//                        .param("text", text)
//                )
//                .call();
        String aiReply = response.getResult().getOutput().getText();
        model.addAttribute("response", aiReply);
        model.addAttribute("text", text);
        return "chat";
    }

    @GetMapping("/chat/{chatId}")
    public String chatMemory(@PathVariable String chatId, @RequestParam String text, Model model) {
        chatMemory.add(chatId, new UserMessage(text));
        String reply = chatClient.prompt()
                .messages(chatMemory.get(chatId))
                .call()
                .content();
        model.addAttribute("memoryResponse", reply);
        return "chat";
    }

}
