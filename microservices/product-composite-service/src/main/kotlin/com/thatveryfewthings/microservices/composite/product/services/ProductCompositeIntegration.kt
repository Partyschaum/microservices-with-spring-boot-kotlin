package com.thatveryfewthings.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.product.ProductService
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.recommendation.RecommendationService
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.core.review.ReviewService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.exceptions.NotFoundException
import com.thatveryfewthings.api.http.HttpErrorInfo
import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.IOException

@Component
class ProductCompositeIntegration(
    webClientBuilder: WebClient.Builder,
    private val mapper: ObjectMapper,
    configuration: ConfigurationProperties,
) : ProductService, RecommendationService, ReviewService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = webClientBuilder.build()

    private val productServiceUrl = "${configuration.productService.url}/product"
    private val recommendationServiceUrl = "${configuration.recommendationService.url}/recommendation"
    private val reviewServiceUrl = "${configuration.reviewService.url}/review"

    override fun getProduct(productId: Int): Mono<Product> {
        val url = "$productServiceUrl/$productId"
        log.debug("Will call the getProduct API on URL: $url")

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(Product::class.java)
            .log()
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
    }

    override fun createProduct(product: Product): Mono<Product> {
        val url = productServiceUrl
        log.debug("Will call the createProduct API on URL: $url")

        return webClient
            .post()
            .uri(url)
            .body(product.toMono(), Product::class.java)
            .retrieve()
            .bodyToMono(Product::class.java)
            .log()
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
    }

    override fun deleteProduct(productId: Int): Mono<Void> {
        val url = "$productServiceUrl/$productId"
        log.debug("Will call the deleteProduct API on URL: $url")

        return webClient
            .delete()
            .uri(url)
            .retrieve()
            .bodyToMono(Void::class.java)
            .log()
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation> {
        val url = "$recommendationServiceUrl?productId=$productId"
        log.debug("Will call the getRecommendations API on URL: $url")

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToFlux(Recommendation::class.java)
            .log()
            .onErrorResume { Flux.empty() }
    }

    override fun createRecommendation(recommendation: Recommendation): Mono<Recommendation> {
        val url = recommendationServiceUrl
        log.debug("Will call the createRecommendation API on URL: $url")

        return webClient
            .post()
            .uri(url)
            .body(recommendation.toMono(), Recommendation::class.java)
            .retrieve()
            .bodyToMono(Recommendation::class.java)
            .log()
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
    }

    override fun deleteRecommendations(productId: Int): Flux<Void> {
        val url = "$recommendationServiceUrl?productId=$productId"
        log.debug("Will call the deleteRecommendations API on URL: $url")

        return webClient
            .delete()
            .uri(url)
            .retrieve()
            .bodyToFlux(Void::class.java)
            .log()
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
    }

    override fun getReviews(productId: Int): Flux<Review> {
        val url = "$reviewServiceUrl?productId=$productId"
        log.debug("Will call getReviews API on URL: $url")

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToFlux(Review::class.java)
            .log()
            .onErrorResume { Flux.empty() }
    }

    override fun createReview(review: Review): Mono<Review> {
        val url = reviewServiceUrl
        log.debug("Will call the createReview API on URL: $url")

        return webClient
            .post()
            .uri(url)
            .body(review.toMono(), Review::class.java)
            .retrieve()
            .bodyToMono(Review::class.java)
            .log()
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
    }

    override fun deleteReviews(productId: Int): Flux<Unit> {
        val url = "$reviewServiceUrl?productId=$productId"
        log.debug("Will call the deleteReviews API on URL: $url")

        return webClient
            .delete()
            .uri(url)
            .retrieve()
            .bodyToFlux(Unit::class.java)
            .log()
            .onErrorMap(WebClientResponseException::class.java) {
                handleWebClientResponseException(it)
            }
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
}
