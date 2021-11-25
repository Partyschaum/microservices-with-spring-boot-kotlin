package com.thatveryfewthings.microservices.core.review

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

private val logger = LoggerFactory.getLogger(ReviewServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
class ReviewServiceApplication

fun main(args: Array<String>) {
    runApplication<ReviewServiceApplication>(*args).apply {
        val mySqlUrl = environment.getProperty("spring.datasource.url")
        logger.info("Connected to MySQL: $mySqlUrl")
    }
}
