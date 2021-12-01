package com.thatveryfewthings.microservices.core.review

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import reactor.core.scheduler.Schedulers

private val logger = LoggerFactory.getLogger(ReviewServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
class ReviewServiceApplication(
    @Value("\${app.jdbc-thread-pool-size}")
    private val threadPoolSize: Int,
    @Value("\${app.jdbc-task-queue-size}")
    private val taskQueueSize: Int,
) {

    @Bean
    fun jdbcScheduler() = Schedulers.newBoundedElastic(
        threadPoolSize,
        taskQueueSize,
        "jdbc-pool"
    )
}

fun main(args: Array<String>) {
    runApplication<ReviewServiceApplication>(*args).apply {
        val mySqlUrl = environment.getProperty("spring.datasource.url")
        logger.info("Connected to MySQL: $mySqlUrl")
    }
}
