package com.thatveryfewthings.microservices.core.recommendation

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

private val logger = LoggerFactory.getLogger(RecommendationServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
class RecommendationServiceApplication

fun main(args: Array<String>) {
    runApplication<RecommendationServiceApplication>(*args).apply {
        val mongoDbHost = environment.getProperty("spring.data.mongodb.host")
        val mongoDbPort = environment.getProperty("spring.data.mongodb.port")
        logger.info("Connected to MongoDB: $mongoDbHost:$mongoDbPort")
    }
}
