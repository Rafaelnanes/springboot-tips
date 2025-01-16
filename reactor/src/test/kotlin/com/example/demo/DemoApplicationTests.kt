package com.example.demo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

	private lateinit var webClient: WebClient

	@Autowired
	private lateinit var chatController: ChatController

	@LocalServerPort
	private val port = 0

	@BeforeEach
	fun setUp(){
		webClient = WebClient.builder()
			.baseUrl("http://localhost:$port")
			.build()
	}

	@Test
	fun `simple case of consuming a SSE endpoint`() {
		val eventFlux = webClient.get()
			.uri("/stream-stuff")
			.accept(MediaType.TEXT_EVENT_STREAM)
			.retrieve()
			.bodyToFlux(String::class.java)

		StepVerifier.create(eventFlux)
			.expectNext("""{"id":"1","message":"myMessage1"}""")
			.expectNext("""{"id":"2","message":"myMessage2"}""")
			.expectComplete()
			.verify(Duration.ofSeconds(5))
	}

}
