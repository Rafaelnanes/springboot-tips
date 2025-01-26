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

    @ExceptionHandler(RuntimeException::class)
    fun handleUserNotFoundException(ex: RuntimeException): ResponseEntity<String> {
        println("Stepping in exception handler: $ex")
        return ResponseEntity.internalServerError().body("My Internal error")
    }
}