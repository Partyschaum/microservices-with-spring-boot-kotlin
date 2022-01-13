package com.thatveryfewthings.springcloud.authorizationserver.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
data class ConfigurationProperties(
    val writerSecret: String,
    val readerSecret: String,
)
