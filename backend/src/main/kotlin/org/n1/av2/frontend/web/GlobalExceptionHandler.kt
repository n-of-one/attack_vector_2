package org.n1.av2.frontend.web

import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.resource.NoResourceFoundException


@ControllerAdvice
@RestController
class GlobalExceptionHandler {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception?): ResponseEntity<String> {
        logger.error("Unhandled exception", e)
        return ResponseEntity("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<String> {
        logger.error("Access denied", e)
        // Handle security-related exceptions here
        return ResponseEntity("Access Denied", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<String> {
        val message = e.bindingResult.allErrors.map { it.defaultMessage}.joinToString(", ")
        logger.error("Bad request: $message", e)
        // Handle validation exceptions here
        return ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(ErrorResponseException::class)
    fun handleException(e: ErrorResponseException): ResponseEntity<String> {
        logger.error("Error", e)

        // Handle validation exceptions here
        return ResponseEntity(e.body.detail, e.statusCode)
    }

    private fun parseArgument(argument: Any?): Any? {
        if (argument is String) {
            return argument.limit(100)
        }
        return argument
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleException(e: HandlerMethodValidationException, request: HttpServletRequest): ResponseEntity<String> {
        // Handle validation exceptions here
        val message = e.allValidationResults.map {
            val errorMessage =  it.resolvableErrors.map{
                it.defaultMessage
            }.joinToString(", ")
            val argument = parseArgument(it.argument)
            val paramName = it.methodParameter.parameterName
            "$paramName: $errorMessage for \"$argument\""
        }.joinToString(", ")




        logger.warn{ "${request.requestURL.toString().limit(100)} -> $message" }
        return ResponseEntity(message, e.statusCode)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleException(e: NoResourceFoundException): ResponseEntity<String> {
        // Handle validation exceptions here
        return ResponseEntity(e.body.detail, e.statusCode)
    }

}

fun String.limit(length: Int): String {
    if (this.length > length) {
        return this.substring(0, length) + "..."
    } else {
        return this
    }
}
