package com.example.demo

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier


@SpringBootTest
class UsingWebTestClientTest {

    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var exampleController: ExampleController

    @BeforeEach
    fun beforeEach() {
        webTestClient = WebTestClient.bindToController(exampleController).build()
    }

    @Nested
    inner class SuccessSuit {

        @Test
        fun `success stream`() {
            val response = webTestClient.get()
                .uri("/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)
                .responseBody

            StepVerifier.create(response)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectNext("""{"id":"1","message":"myMessage1"}""")
                .expectNext("""{"id":"2","message":"myMessage2"}""")
                .expectComplete()
                .verify()
        }

        @Test
        fun `stream using jsonPath`() {
            val response = webTestClient.get()
                .uri("/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)
                .responseBody

            StepVerifier.create(response)
                .expectNextMatches { value ->
                    val idValue = JsonPath.parse(value).read<String>("$.id")
                    return@expectNextMatches "0" == idValue
                }
                .expectNextMatches { value ->
                    val idValue = JsonPath.parse(value).read<String>("$.id")
                    return@expectNextMatches "1" == idValue
                }
                .expectNextMatches { value ->
                    val idValue = JsonPath.parse(value).read<String>("$.id")
                    return@expectNextMatches "2" == idValue
                }
                .expectComplete()
                .verify()
        }

        @Test
        fun `no stream`() {
            val response = webTestClient.get()
                .uri("/simple")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)
                .responseBody

            StepVerifier.create(response)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectNext("""{"id":"1","message":"myMessage1"}""")
                .expectNext("""{"id":"2","message":"myMessage2"}""")
                .expectComplete()
                .verify()
        }

        @Test
        fun `using-sse-class`() {
            val response = webTestClient.get()
                .uri("/using-sse-class")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(String::class.java)
                .responseBody

            StepVerifier.create(response)
                .expectNext("""{"id":"0","message":"myMessage0"}""")
                .expectNext("""{"id":"1","message":"myMessage1"}""")
                .expectNext("""{"id":"2","message":"myMessage2"}""")
                .expectComplete()
                .verify()
        }

        @Test
        fun `using-sse-class and testing event type`() {
            val responseType = object : ParameterizedTypeReference<ServerSentEvent<String>>() {}
            val response = webTestClient.get()
                .uri("/using-sse-class-error")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult(responseType)
                .responseBody

            StepVerifier.create(response)
                .expectNext(
                    ServerSentEvent.builder<String>().event("success")
                        .data("""{"id":"0","message":"myMessage0"}""").build()
                )
                .expectNext(
                    ServerSentEvent.builder<String>().event("error")
                        .data("""{"id":null,"message":null}""").build()
                )
                .expectNext(
                    ServerSentEvent.builder<String>().event("error")
                        .data("""{"id":null,"message":null}""").build()
                )
                .expectNext(
                    ServerSentEvent.builder<String>().event("success")
                        .data("""{"id":"3","message":"myMessage3"}""").build()
                )
                .verifyComplete()
        }

    }

    @Nested
    inner class ErrorSuit {
        @Test
        fun `verity assertion error stream`() {
            val exception = assertThrows<AssertionError> {
                val response = webTestClient.get()
                    .uri("/stream")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk
                    .returnResult(String::class.java)
                    .responseBody

                StepVerifier.create(response)
                    .expectNext("""{"id":"1","message":"myMessage1"}""") // should be {"id":"0","message":"myMessage0"}
                    .expectComplete()
                    .verify()
            }
            Assertions.assertNotNull(exception)
        }

    }

}