package com.thatveryfewthings.microservices.core.recommendation.config

import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.recommendation.RecommendationService
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.event.Event.Type.CREATE
import com.thatveryfewthings.api.event.Event.Type.DELETE
import com.thatveryfewthings.api.exceptions.EventProcessingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

typealias MessageProcessor = Consumer<Event<Int, Recommendation?>>

@Configuration
class MessageProcessorConfig(
    @Autowired
    private val recommendationService: RecommendationService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun messageProcessor(): MessageProcessor = MessageProcessor { event ->
        log.info("Process message created at ${event.eventCreatedAt}...")

        when (event.eventType) {
            CREATE -> {
                checkNotNull(event.data).let {
                    log.info("Create recommendation with ID: ${it.productId}/${it.recommendationId}")
                    recommendationService.createRecommendation(it).block()
                }
            }
            DELETE -> {
                log.info("Delete recommendations with ID: ${event.key}")
                recommendationService.deleteRecommendations(event.key).block()
            }
            else -> {
                throw EventProcessingException(
                    "Incorrect event type: ${event.eventType}, expected a CREATE or DELETE event"
                )
            }
        }
    }
}
