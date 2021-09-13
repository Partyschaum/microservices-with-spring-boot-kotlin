package com.thatveryfewthings.microservices.composite.product

import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
@EnableConfigurationProperties(ConfigurationProperties::class)
class ProductCompositeServiceApplication {

    @Bean
    fun restTemplate() = RestTemplate()
}

fun main(args: Array<String>) {
    runApplication<ProductCompositeServiceApplication>(*args)
}
