package spring.ai.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ToolController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider tool;

    @PostMapping("/tools")
    public String aiSearch(@RequestParam String text) {

        return chatClient.prompt()
                .tools(tool)
                .user(text)
                .call()
                .content();
    }

}
