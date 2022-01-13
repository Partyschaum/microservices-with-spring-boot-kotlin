package com.thatveryfewthings.springcloud.eurekaserver.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
data class ConfigurationProperties(
    val eurekaUsername: String,
    val eurekaPassword: String,
)
