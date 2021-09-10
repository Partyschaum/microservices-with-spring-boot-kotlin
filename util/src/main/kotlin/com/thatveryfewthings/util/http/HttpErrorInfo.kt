package com.thatveryfewthings.util.http

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

data class HttpErrorInfo(
    val timestamp: ZonedDateTime = ZonedDateTime.now(),
    val path: String? = null,
    private val httpStatus: HttpStatus? = null,
    val message: String? = null,
) {

    val status: Int
        get() = httpStatus!!.value()

    val error: String
        get() = httpStatus!!.reasonPhrase
}
