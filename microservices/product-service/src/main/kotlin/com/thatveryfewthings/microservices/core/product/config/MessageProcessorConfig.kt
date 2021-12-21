package com.thatveryfewthings.microservices.core.product.config

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.product.ProductService
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.event.Event.Type.CREATE
import com.thatveryfewthings.api.event.Event.Type.DELETE
import com.thatveryfewthings.api.exceptions.EventProcessingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

typealias MessageProcessor = Consumer<Event<Int, Product?>>

@Configuration
class MessageProcessorConfig(
    @Autowired
    private val productService: ProductService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun messageProcessor(): MessageProcessor = MessageProcessor { event ->
        log.info("Process message created at ${event.eventCreatedAt}...")

        when (event.eventType) {
            CREATE -> {
                checkNotNull(event.data).let {
                    log.info("Create product with ID: ${it.productId}")
                    productService.createProduct(it).block()
                }
            }
            DELETE -> {
                log.info("Delete product with ID: ${event.key}")
                productService.deleteProduct(event.key).block()
            }
            else -> {
                throw EventProcessingException(
                    "Incorrect event type: ${event.eventType}, expected a CREATE or DELETE event"
                )
            }
        }
    }
}
