package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class ChatController() {

    @GetMapping("/stream-stuff", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getMessage(): Flux<EventMessage> {
        return Flux.just(EventMessage(id = "1", message = "myMessage1"), EventMessage(id = "2", message = "myMessage2"))
    }

    data class EventMessage(val id: String, val message: String)
}