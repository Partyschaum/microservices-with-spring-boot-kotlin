package com.thatveryfewthings.microservices.composite.product.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "api")
data class ApiProperties(
    val common: Common,
    val responseCodes: ResponseCodes,
) {

    data class Common(
        val version: String,
        val title: String,
        val description: String,
        val termsOfService: String,
        val license: String,
        val licenseUrl: String,
        val externalDocDesc: String,
        val externalDocUrl: String,
        val contact: Contact,
    )

    data class Contact(
        val name: String,
        val url: String,
        val email: String,
    )

    data class ResponseCodes(
        val ok: String,
        val badRequest: String,
        val notFound: String,
        val unprocessableEntity: String,
    )
}
