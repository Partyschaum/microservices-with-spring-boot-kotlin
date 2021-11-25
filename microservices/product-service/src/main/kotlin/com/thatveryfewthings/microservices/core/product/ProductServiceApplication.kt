package com.thatveryfewthings.microservices.core.product

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

private val logger = LoggerFactory.getLogger(ProductServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
class ProductServiceApplication

fun main(args: Array<String>) {
    runApplication<ProductServiceApplication>(*args).apply {
        val mongoDbHost = environment.getProperty("spring.data.mongodb.host")
        val mongoDbPort = environment.getProperty("spring.data.mongodb.port")
        logger.info("Connected to MongoDB: $mongoDbHost:$mongoDbPort")
    }
}
