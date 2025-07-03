package spring.ai.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.ai.demo.tool.VectorMetadataTool;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .build();
    }


    @Bean
    ToolCallbackProvider toolCallbackProvider(VectorMetadataTool getDataTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(getDataTool)
                .build();
    }

    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

}

