package com.thatveryfewthings.springcloud.authorizationserver

import com.thatveryfewthings.springcloud.authorizationserver.properties.ConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ConfigurationProperties::class)
class AuthorizationServerApplication

fun main(args: Array<String>) {
    runApplication<AuthorizationServerApplication>(*args)
}
