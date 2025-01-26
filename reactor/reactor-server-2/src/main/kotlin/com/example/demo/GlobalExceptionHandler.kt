package com.example.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

//    @ExceptionHandler(RuntimeException::class)
//    fun handleUserNotFoundException(ex: RuntimeException): Mono<ResponseEntity<String>> {
//        println("Stepping in exception handler")
//        return Mono.just(ResponseEntity.internalServerError().body("My Internal error"))
//    }

    @ExceptionHandler(MyInternalException::class)
    fun internalError(ex: MyInternalException): ResponseEntity<Response> {
        println("Stepping in exception handler: $ex")
        val message: String = if (ex.message == null) "Unknown error" else ex.message.toString()
        return ResponseEntity.badRequest().body(Response(message))
    }

    class Response(val value: String)
}