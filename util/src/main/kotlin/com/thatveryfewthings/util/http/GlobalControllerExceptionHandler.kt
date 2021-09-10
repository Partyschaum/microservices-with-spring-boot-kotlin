package com.thatveryfewthings.util.http

import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
internal class GlobalControllerExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        NotFoundException::class
    )
    @ResponseBody
    fun handleNotFoundExceptions(request: ServerHttpRequest, ex: NotFoundException): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, ex)
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(
        InvalidInputException::class
    )
    @ResponseBody
    fun handleInvalidInputException(request: ServerHttpRequest, ex: InvalidInputException): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex)
    }

    private fun createHttpErrorInfo(httpStatus: HttpStatus, request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        val path = request.path.pathWithinApplication().value()
        val message = ex.message

        log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message)

        return HttpErrorInfo(
            path = path,
            httpStatus = httpStatus,
            message = message,
        )
    }
}
