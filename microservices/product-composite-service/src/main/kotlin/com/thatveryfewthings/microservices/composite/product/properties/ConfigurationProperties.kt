package com.thatveryfewthings.microservices.composite.product.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
data class ConfigurationProperties(
    val productService: Service,
    val recommendationService: Service,
    val reviewService: Service,
) {

    data class Service(
        val host: String,
        val port: Int,
        val https: Boolean = false,
    ) {
        val url: String
            get() = "${if (https) "https" else "http"}://$host:$port"
    }
}
