package com.example.demo

import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration

@RestController
class ExampleController {

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): Flux<EventMessage> {
        return getDefaultFlux()
            .delayElements(Duration.ofSeconds(1))
    }

    @GetMapping("/simple")
    fun simple(): Flux<EventMessage> {
        return getDefaultFlux()
            .delayElements(Duration.ofSeconds(1))
    }


    @GetMapping("/using-sse-class", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sseClass(): Flux<ServerSentEvent<EventMessage>> {
        return getDefaultFlux()
            .map { data ->
                ServerSentEvent.builder<EventMessage>()
                    .data(data)
                    .build()
            }
    }

    @GetMapping("/using-sse-class-error", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
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

    //    @GetMapping("/text-stream-error", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @GetMapping("/text-stream-error")
    fun getMessageError(): MutableList<EventMessage>? {
        return getFluxWithInvalidObjects()
            .map {
                println("Map: $it")
                if (it!!.id == null) {
                    println("Found null")
//                    throw RuntimeException("Missing id")
//                    return@map EventMessage(id = "99", message = "myMessage99")
                }
                return@map it
            }
            .onErrorContinue { ex, data ->
                println("onErrorContinue: Ignoring event with missing id-> $data, error: $ex")
//                throw ex
            }
            .doOnError {
                println("doOnError")
//                throw RuntimeException("doOnError")
            }
//            .onErrorResume {
//                println("onErrorResume")
////                Flux.empty<EventMessage>()
//                Flux.error(it) // Relança a exceção
//            }
            .collectList()
            .block()
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