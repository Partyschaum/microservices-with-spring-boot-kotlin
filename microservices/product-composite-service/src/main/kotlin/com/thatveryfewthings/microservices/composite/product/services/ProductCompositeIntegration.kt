package com.thatveryfewthings.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.product.ProductService
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.recommendation.RecommendationService
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.core.review.ReviewService
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.event.Event.Type.CREATE
import com.thatveryfewthings.api.event.Event.Type.DELETE
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.exceptions.NotFoundException
import com.thatveryfewthings.api.http.HttpErrorInfo
import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.io.IOException
import java.util.logging.Level

@Component
class ProductCompositeIntegration(
    webClientBuilder: WebClient.Builder,
    configuration: ConfigurationProperties,
    private val mapper: ObjectMapper,
    private val streamBridge: StreamBridge,
    private val publishEventScheduler: Scheduler,
) : ProductService, RecommendationService, ReviewService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = webClientBuilder.build()

    private val productServiceHost = configuration.productService.url
    private val recommendationServiceHost = configuration.recommendationService.url
    private val reviewServiceHost = configuration.reviewService.url

    private val productServiceEndpoint = "$productServiceHost/product"
    private val recommendationServiceEndpoint = "$recommendationServiceHost/recommendation"
    private val reviewServiceEndpoint = "$reviewServiceHost/review"

    override fun getProduct(productId: Int): Mono<Product> {
        val url = "$productServiceEndpoint/$productId"
        log.debug("Will call the getProduct API on URL: $url")

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(Product::class.java)
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
            .log(log.name, Level.FINE)
    }

    override fun createProduct(product: Product): Mono<Product> {
        log.debug("Emit CREATE event for product with productId ${product.productId}")

        return Mono.fromCallable {
            sendMessage(
                bindingName = "products-out-0",
                event = Event(CREATE, product.productId, product),
            )
            product
        }.subscribeOn(publishEventScheduler)
            .log(log.name, Level.FINE)
    }

    override fun deleteProduct(productId: Int): Mono<Void> {
        log.debug("Emit DELETE event for product with productId $productId")

        return Mono.fromCallable {
            sendMessage(
                bindingName = "products-out-0",
                event = Event(DELETE, productId, null),
            )
        }.subscribeOn(publishEventScheduler)
            .then()
            .log(log.name, Level.FINE)
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation> {
        val url = "$recommendationServiceEndpoint?productId=$productId"
        log.debug("Will call the getRecommendations API on URL: $url")

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToFlux(Recommendation::class.java)
            .onErrorResume { Flux.empty() }
            .log(log.name, Level.FINE)
    }

    override fun createRecommendation(recommendation: Recommendation): Mono<Recommendation> {
        log.debug("Emit CREATE event for recommendation with productId ${recommendation.productId}")

        return Mono.fromCallable {
            sendMessage(
                bindingName = "recommendations-out-0",
                event = Event(CREATE, recommendation.productId, recommendation),
            )
            recommendation
        }.subscribeOn(publishEventScheduler)
            .log(log.name, Level.FINE)
    }

    override fun deleteRecommendations(productId: Int): Mono<Void> {
        log.debug("Emit DELETE event for recommendation with productId $productId")

        return Mono.fromCallable {
            sendMessage(
                bindingName = "recommendations-out-0",
                event = Event(DELETE, productId, null),
            )
        }.subscribeOn(publishEventScheduler)
            .then()
            .log(log.name, Level.FINE)
    }

    override fun getReviews(productId: Int): Flux<Review> {
        val url = "$reviewServiceEndpoint?productId=$productId"
        log.debug("Will call getReviews API on URL: $url")

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToFlux(Review::class.java)
            .onErrorResume { Flux.empty() }
            .log(log.name, Level.FINE)
    }

    override fun createReview(review: Review): Mono<Review> {
        log.debug("Emit CREATE event for review with productId ${review.productId}")

        return Mono.fromCallable {
            sendMessage(
                bindingName = "reviews-out-0",
                event = Event(CREATE, review.productId, review),
            )
            review
        }.subscribeOn(publishEventScheduler)
            .log(log.name, Level.FINE)
    }

    override fun deleteReviews(productId: Int): Mono<Void> {
        log.debug("Emit DELETE event for review with productId $productId")

        return Mono.fromRunnable<Unit> {
            sendMessage(
                bindingName = "reviews-out-0",
                event = Event(DELETE, productId, null),
            )
        }.subscribeOn(publishEventScheduler)
            .then()
            .log(log.name, Level.FINE)
    }

    fun getProductHealth() = getHealth(productServiceHost)
    fun getRecommendationHealth() = getHealth(recommendationServiceHost)
    fun getReviewHealth() = getHealth(reviewServiceHost)

    fun getHealth(url: String): Mono<Health> {
        val actuatorUrl = "$url/actuator/health"
        log.debug("Will call the health API on URL: $url")

        return webClient
            .get()
            .uri(actuatorUrl)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { Health.Builder().up().build() }
            .onErrorResume { Mono.just(Health.Builder().down(it).build()) }
            .log(log.name, Level.FINE)
    }

    private fun handleWebClientResponseException(exception: WebClientResponseException): RuntimeException {
        return when (exception.statusCode) {
            NOT_FOUND -> NotFoundException(exception.errorMessage)
            UNPROCESSABLE_ENTITY -> InvalidInputException(exception.errorMessage)

            else -> {
                log.warn("Got an unexpected HTTP error: ${exception.statusCode}, will rethrow it")
                log.warn("Error body: ${exception.responseBodyAsString}")
                exception
            }
        }
    }

    private val WebClientResponseException.errorMessage: String
        get() = try {
            mapper.readValue(responseBodyAsString, HttpErrorInfo::class.java).message
                ?: responseBodyAsString
        } catch (ex: IOException) {
            message ?: responseBodyAsString
        }

    private fun <K, T> sendMessage(bindingName: String, event: Event<K, T>) {
        val message = MessageBuilder.withPayload(event)
            .setHeader("partitionKey", event.key)
            .build()

        streamBridge.send(bindingName, message)
    }
}
