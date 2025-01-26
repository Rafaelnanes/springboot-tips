package com.example.demo

import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

//@Configuration(proxyBeanMethods = false)
class MyRoutingConfiguration : WebFluxConfigurer {

    @Bean
    fun monoRouterFunction(userHandler: ExampleHandler): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            GET("/stream").and(ACCEPT_JSON), userHandler::stream
        )
    }

    companion object {
        private val ACCEPT_JSON = accept(MediaType.TEXT_EVENT_STREAM)
    }

}