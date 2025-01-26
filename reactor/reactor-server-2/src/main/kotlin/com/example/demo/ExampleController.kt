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

    @GetMapping("/simple-error/case-1", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getMessageError(): Flux<EventMessage> {
        if (true) {
            throw MyInternalException("First")
        }
        return defaultFluxError()
    }

    @GetMapping("/simple-error/case-2", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun case2(): Flux<EventMessage> {
        return defaultFluxError()
            .onErrorContinue { ex, data ->
                println("onErrorContinue: Ignoring event with missing id-> $data, error: $ex")
                throw ex
            }
            .onErrorResume {
                println("onErrorResume")
                return@onErrorResume Flux.empty()
            }
    }

    @GetMapping("/simple-error/case-3", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun case3(): Flux<EventMessage> {
        return defaultFluxError()
            .onErrorContinue { ex, data ->
                println("onErrorContinue: Ignoring event with missing id-> $data, error: $ex")
                throw ex
            }
            .onErrorResume {
                println("onErrorResume")
                return@onErrorResume Flux.just(EventMessage())
            }
    }

    @GetMapping("/simple-error/case-4", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun case4(): Flux<EventMessage> {
        return defaultFluxError()
            .onErrorContinue { ex, data ->
                println("onErrorContinue: Ignoring event with missing id-> $data, error: $ex")
//                throw ex
            }
            .onErrorResume {
                // won't be printed
                println("onErrorResume")
                return@onErrorResume Flux.just(EventMessage())
            }
    }

    @GetMapping("/simple-error/case-5", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun case5(): Flux<EventMessage> {
        return defaultFluxError()
            .doOnError {
                println("doOnError")
                throw MyInternalException("doOnError")
            }
            .onErrorResume {
                println("onErrorResume")
                return@onErrorResume Flux.just(EventMessage())
            }
    }

    @GetMapping("/simple-error/case-6", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun case6(): Flux<EventMessage> {
        return defaultFluxError()
            .onErrorResume {
                println("onErrorResume")
                return@onErrorResume Flux.error(it)
            }
    }

    private fun defaultFluxError(): Flux<EventMessage> {
        val flux = getFluxWithInvalidObjects()
            .map {
                if (it!!.id == null) {
                    throw MyInternalException("Missing id")
                }
                return@map it
            }
        return flux
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