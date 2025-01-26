package com.example.demo

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/integration")
class IntegrationController {

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): Flux<EventMessage> {
//        return WebClient.create("http://localhost:8089/simple-error/case-1")
        return WebClient.create("http://localhost:8089")
            .get()
            .uri("/stream")
            .retrieve()
            .onStatus({ it.is4xxClientError }, {
                println("onStatus: is4xxClientError")
                return@onStatus Mono.error(MyInternalException("Integration error on status 400"))
            })
            .bodyToFlux(EventMessage::class.java)
            .doOnEach {
                println("doOnEach: ${it.get()}")
            }
            .doOnError {
                println("doOnError: ${it.message}")
            }
//            .onErrorComplete() // suppresses the exception by closing the stream
            .onErrorResume({ it !is MyInternalException }, {
                println("onErrorResume: ${it.message}")
                return@onErrorResume Flux.error(MyInternalException("Integration error"))
            })
    }

    data class EventMessage(val id: String? = null, val message: String? = null)
}