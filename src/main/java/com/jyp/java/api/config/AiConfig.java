package com.jyp.java.api.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // 这个 Bean 的作用是：每当 AI 服务接收到一个新的 memoryId 时，
    // 如果内存里没有这个 ID，就创建一个新的 MessageWindowChatMemory (容量为10条)
    @Bean
    ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(10);
    }
}
