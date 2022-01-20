package com.thatveryfewthings.microservices.composite.product

import com.thatveryfewthings.microservices.composite.product.properties.ApiProperties
import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import reactor.core.scheduler.Schedulers

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
@EnableConfigurationProperties(ConfigurationProperties::class, ApiProperties::class)
class ProductCompositeServiceApplication(
    @Value("\${app.publish-event-thread-pool-size}")
    private val threadPoolSize: Int,
    @Value("\${app.publish-event-task-queue-size}")
    private val taskQueueSize: Int,
) {

    @Bean
    fun publishEventScheduler() = Schedulers.newBoundedElastic(
        threadPoolSize,
        taskQueueSize,
        "publish-event-pool"
    )
}

fun main(args: Array<String>) {
    runApplication<ProductCompositeServiceApplication>(*args)
}
