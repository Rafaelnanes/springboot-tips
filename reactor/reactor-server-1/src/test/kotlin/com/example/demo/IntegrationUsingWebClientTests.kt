package com.example.demo

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationUsingWebClientTests {

    private lateinit var webClient: WebClient
    private var wireMockServer: WireMockServer? = null

    @LocalServerPort
    private val port = 0

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        wireMockServer!!.start()
        configureFor("localhost", 8089)

        webClient = WebClient.builder()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @AfterEach
    fun afterEach() {
        wireMockServer!!.stop()
    }

    @Test
    fun `stream`() {
        val eventStream = """
        data: {"id":"0","message":"myMessage0"}

        data: {"id":"1","message":"myMessage1"}
        
        data: {"id":"2","message":"myMessage2"}
        
        """.trimIndent()
        stubFor(
            get(urlEqualTo("/stream"))
                .willReturn(
                    okForContentType(MediaType.TEXT_EVENT_STREAM_VALUE, eventStream)
                        .withTransformers("sse-transformer")
                )
        )

        val eventFlux = webClient.get()
            .uri("/integration/stream")
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

    @Test
    fun `stream error 400`() {
        val body = """{"message":"My error"}""".trimIndent()
        stubFor(
            get(urlEqualTo("/stream"))
                .willReturn(
                    badRequest()
                        .withBody(body)
                )
        )

        val eventFlux = webClient.get()
            .uri("/integration/stream")
            .retrieve()
            .bodyToFlux(String::class.java)

        StepVerifier.create(eventFlux)
            .expectErrorMatches {
                it is WebClientResponseException
                        && it.statusCode == HttpStatus.BAD_REQUEST
                        && it.responseBodyAsString == """{"value":"Integration error on status 400"}"""
            }
            .verify(Duration.ofSeconds(5))
    }

    @Test
    fun `stream error 500`() {
        val body = """{"message":"My error"}""".trimIndent()
        stubFor(
            get(urlEqualTo("/stream"))
                .willReturn(
                    status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withBody(body)
                )
        )

        val eventFlux = webClient.get()
            .uri("/integration/stream")
            .retrieve()
            .bodyToFlux(String::class.java)

        StepVerifier.create(eventFlux)
            .expectErrorMatches {
                it is WebClientResponseException
                        && it.statusCode == HttpStatus.BAD_REQUEST
                        && it.responseBodyAsString == """{"value":"Integration error on status 500"}"""
            }
            .verify(Duration.ofSeconds(5))
    }

}
