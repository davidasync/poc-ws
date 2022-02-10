package com.github.davidasync.poc.ws

import org.redisson.api.RTopicReactive
import org.redisson.api.RedissonReactiveClient
import org.redisson.client.codec.StringCodec
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import java.net.URI

@Service
class ChatRoomService(private val client: RedissonReactiveClient) : WebSocketHandler {
    override fun handle(webSocketSession: WebSocketSession): Mono<Void> {
        val room = getChatRoomName(webSocketSession)
        val topic: RTopicReactive = this.client.getTopic(room, StringCodec.INSTANCE)
        val list = client.getList<String>("history:$room", StringCodec.INSTANCE)

        // subscribe
        webSocketSession.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .flatMap { msg -> list.add(msg).then(topic.publish(msg)) }
            .doOnError(System.out::println)
            .doFinally { s -> println("Subscriber finally $s") }
            .subscribe()

        // publisher
        val flux: Flux<WebSocketMessage> = topic.getMessages(String::class.java)
            .startWith(list.iterator())
            .map(webSocketSession::textMessage)
            .doOnError { x: Throwable? -> println(x) }
            .doFinally { s: SignalType -> println("publisher finally $s") }

        return webSocketSession.send(flux)
    }

    private fun getChatRoomName(socketSession: WebSocketSession): String? {
        val uri: URI = socketSession.handshakeInfo.uri
        return UriComponentsBuilder.fromUri(uri)
            .build()
            .queryParams
            .toSingleValueMap()
            .getOrDefault("room", "default")
    }
}