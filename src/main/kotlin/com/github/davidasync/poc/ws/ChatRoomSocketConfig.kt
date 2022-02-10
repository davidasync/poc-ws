package com.github.davidasync.poc.ws

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class ChatRoomSocketConfig {
    @Bean
    fun handlerMapping(chatRoomService: ChatRoomService): HandlerMapping {
        val map = mapOf(
            "/chat" to chatRoomService
        )

        return SimpleUrlHandlerMapping(map, -1)
    }
}
