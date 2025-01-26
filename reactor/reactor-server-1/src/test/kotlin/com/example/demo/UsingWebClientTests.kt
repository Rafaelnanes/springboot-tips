package com.example.demo

import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsingWebClientTests {

    private lateinit var webClient: WebClient

    @LocalServerPort
    private val port = 0

    @BeforeEach
    fun setUp() {
        webClient = WebClient.builder()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Nested
    inner class SuccessSuit {
        @Test
        fun `stream`() {
            val eventFlux = webClient.get()
                .uri("/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String::class.java)

            StepVerifier.create(eventFlux)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectNext("""{"id":"1","message":"myMessage1"}""")
                .expectNext("""{"id":"2","message":"myMessage2"}""")
                .expectComplete()
                .verify(Duration.ofSeconds(5))
        }
    }

    @Nested
    inner class ErrorSuit {
        @Test
        fun `case 1`() {
            val eventFlux = webClient.get()
                .uri("/simple-error/case-1")
                .retrieve()
                .bodyToFlux(String::class.java)

            StepVerifier.create(eventFlux)
                .expectErrorMatches {
                    it is WebClientResponseException
                            && it.statusCode == HttpStatus.BAD_REQUEST
                            && it.responseBodyAsString == """{"value":"First"}"""
                }
                .verify(Duration.ofSeconds(5))
        }

        @Test
        fun `case 2`() {
            val eventFlux = webClient.get()
                .uri("/simple-error/case-2")
                .retrieve()
                .bodyToFlux(String::class.java)

            StepVerifier.create(eventFlux)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectComplete()
                .verify(Duration.ofSeconds(5))
        }

        @Test
        fun `case 3`() {
            val eventFlux = webClient.get()
                .uri("/simple-error/case-3")
                .retrieve()
                .bodyToFlux(String::class.java)

            StepVerifier.create(eventFlux)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectNext("""{"id":null,"message":null}""")
                .expectComplete()
                .verify(Duration.ofSeconds(5))
        }

        @Test
        fun `case 4`() {
            val eventFlux = webClient.get()
                .uri("/simple-error/case-4")
                .retrieve()
                .bodyToFlux(String::class.java)

            StepVerifier.create(eventFlux)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectNext("""{"id":"3","message":"myMessage3"}""")
                .expectComplete()
                .verify(Duration.ofSeconds(5))
        }

        @Test
        fun `case 5`() {
            val eventFlux = webClient.get()
                .uri("/simple-error/case-5")
                .retrieve()
                .bodyToFlux(String::class.java)

            StepVerifier.create(eventFlux)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectNext("""{"id":null,"message":null}""")
                .expectComplete()
                .verify(Duration.ofSeconds(5))
        }

        @Test
        fun `case 6`() {
            val exception = assertThrows<AssertionError> {
                val eventFlux = webClient.get()
                    .uri("/simple-error/case-6")
                    .retrieve()
                    .bodyToFlux(String::class.java)

                StepVerifier.create(eventFlux)
                    .expectNext("""{"id":"0","message":"myMessage0"}""")
                    .expectNext("""{"id":null,"message":null}""")
                    .expectComplete()
                    .verify(Duration.ofSeconds(5))
            }
            Assertions.assertNotNull(exception)
            val prematureCloseException: WebClientResponseException =
                exception.suppressedExceptions.get(0) as WebClientResponseException
            Assertions.assertTrue(
                prematureCloseException.message.contains("/simple-error/case-6, but response failed with cause: reactor.netty.http.client.PrematureCloseException: Connection prematurely closed DURING response")
            )

        }
    }

}
