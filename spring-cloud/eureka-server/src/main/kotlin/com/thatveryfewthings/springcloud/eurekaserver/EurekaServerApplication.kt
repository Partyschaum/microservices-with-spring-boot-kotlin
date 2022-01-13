package com.thatveryfewthings.springcloud.eurekaserver

import com.thatveryfewthings.springcloud.eurekaserver.properties.ConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
@EnableConfigurationProperties(ConfigurationProperties::class)
class EurekaServerApplication

fun main(args: Array<String>) {
    runApplication<EurekaServerApplication>(*args)
}
