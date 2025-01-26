package com.example.demo

import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration


@Service
class ExampleHandler {

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(request: ServerRequest): Mono<ServerResponse> {
        val flux = getDefaultFlux()
            .delayElements(Duration.ofSeconds(1))
        return ServerResponse.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(flux, EventMessage::class.java)
    }

    //    @GetMapping("/simple")
    fun simple(): Flux<EventMessage> {
        return getDefaultFlux()
            .delayElements(Duration.ofSeconds(1))
    }


    //    @GetMapping("/using-sse-class", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sseClass(): Flux<ServerSentEvent<EventMessage>> {
        return getDefaultFlux()
            .map { data ->
                ServerSentEvent.builder<EventMessage>()
                    .data(data)
                    .build()
            }
    }

    //    @GetMapping("/using-sse-class-error", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamError(): Flux<ServerSentEvent<EventMessage>> {
        return getFluxWithInvalidObjects()
            .map { data ->
                val builder = ServerSentEvent.builder<EventMessage>()
                builder.event("success")
                if (data.id == null) {
                    builder.event("error")
                }
                builder.data(data)
                    .build()
            }
    }

    //    @GetMapping("/simple-error/case-1", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getMessageError(): Flux<EventMessage> {
        throw MyInternalException("First")
    }

    private fun getFluxWithInvalidObjects() = Flux.just(
        EventMessage(id = "0", message = "myMessage0"),
        EventMessage(),
        EventMessage(),
        EventMessage(id = "3", message = "myMessage3")
    ).delayElements(Duration.ofSeconds(1))

    private fun getDefaultFlux() = Flux.just(
        EventMessage(id = "0", message = "myMessage0"),
        EventMessage(id = "1", message = "myMessage1"),
        EventMessage(id = "2", message = "myMessage2"),
    )

    data class EventMessage(val id: String? = null, val message: String? = null)
}