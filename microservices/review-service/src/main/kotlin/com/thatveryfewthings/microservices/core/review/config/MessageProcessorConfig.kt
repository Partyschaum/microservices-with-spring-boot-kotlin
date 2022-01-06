package com.thatveryfewthings.microservices.core.review.config

import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.core.review.ReviewService
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.exceptions.EventProcessingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

typealias MessageProcessor = Consumer<Event<Int, Review?>>

@Configuration
class MessageProcessorConfig(
    @Autowired
    private val reviewService: ReviewService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun messageProcessor(): MessageProcessor = MessageProcessor { event ->
        log.info("Process message created at ${event.eventCreatedAt}...")

        when (event.eventType) {
            Event.Type.CREATE -> {
                checkNotNull(event.data).let {
                    log.info("Create review with ID: ${it.productId}/${it.reviewId}")
                    reviewService.createReview(it).block()
                }
            }
            Event.Type.DELETE -> {
                log.info("Delete reviews with ID: ${event.key}")
                reviewService.deleteReviews(event.key).block()
            }
            else -> {
                throw EventProcessingException(
                    "Incorrect event type: ${event.eventType}, expected a CREATE or DELETE event"
                )
            }
        }
    }
}
