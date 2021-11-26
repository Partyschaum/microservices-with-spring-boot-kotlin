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
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.IOException

@Component
class ProductCompositeIntegration(
    private val restTemplate: RestTemplate,
    private val mapper: ObjectMapper,
    configuration: ConfigurationProperties,
) : ProductService, RecommendationService, ReviewService {

    private val log = LoggerFactory.getLogger(javaClass)

    private val productServiceUrl = "${configuration.productService.url}/product"
    private val recommendationServiceUrl = "${configuration.recommendationService.url}/recommendation"
    private val reviewServiceUrl = "${configuration.reviewService.url}/review"

    override fun getProduct(productId: Int): Product {
        return try {
            val url = "$productServiceUrl/$productId"
            log.debug("Will call the getProduct API on URL: $url")

            restTemplate.getForObject(url, Product::class.java)?.also {
                log.debug("Found a product with id: $productId")
            } ?: throw NotFoundException("No product found for productId: $productId")

        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun createProduct(product: Product): Product {
        return try {
            val url = productServiceUrl
            log.debug("Will call the createProduct API on URL: $url")

            checkNotNull(
                restTemplate.postForObject(url, product, Product::class.java)?.also {
                    log.debug("Created a product with id: ${it.productId}")
                }
            ) { "Problem when trying to create a product with id ${product.productId} at $url" }

        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun deleteProduct(productId: Int) {
        try {
            val url = "$productServiceUrl/$productId"
            log.debug("Will call the deleteProduct API on URL: $url")

            restTemplate.delete(url)

        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun getRecommendations(productId: Int): List<Recommendation> {
        return try {
            val url = recommendationServiceUrl + productId
            log.debug("Will call the getRecommendations API on URL: $url")

            val responseType = object : ParameterizedTypeReference<List<Recommendation>>() {}
            val recommendations = restTemplate.exchange(url, GET, null, responseType).body

            recommendations?.also {
                log.debug("Found ${it.size} recommendations for the product with id: $productId")
            } ?: emptyList<Recommendation>().also {
                log.warn("No recommendations found for productId: $productId")
            }

        } catch (ex: Exception) {
            log.warn("Got an exception while requesting recommendations, return zero recommendations: ${ex.message}")
            emptyList()
        }
    }

    override fun createRecommendation(recommendation: Recommendation): Recommendation {
        return try {
            val url = recommendationServiceUrl
            log.debug("Will call the createRecommendation API on URL: $url")

            checkNotNull(
                restTemplate.postForObject(url, recommendation, Recommendation::class.java)?.also {
                    log.debug("Created a recommendation with id: ${it.recommendationId}")
                }
            ) { "Problem when trying to create a recommendation for product with id ${recommendation.productId} at $url" }

        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun deleteRecommendations(productId: Int) {
        try {
            val url = "$recommendationServiceUrl?productId=$productId"
            log.debug("Will call the deleteRecommendations API on URL: $url")

            restTemplate.delete(url)

        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun getReviews(productId: Int): List<Review> {
        return try {
            val url = reviewServiceUrl + productId
            log.debug("Will call getReviews API on URL: $url")

            val responseType = object : ParameterizedTypeReference<List<Review>>() {}
            val reviews = restTemplate.exchange(url, GET, null, responseType).body

            reviews?.also {
                log.debug("Found ${it.size} reviews for the product with id: $productId")
            } ?: emptyList<Review>().also {
                log.warn("No reviews found for productId: $productId")
            }

        } catch (ex: Exception) {
            log.warn("Got an exception while requesting reviews, return zero reviews: ${ex.message}")
            emptyList()
        }
    }

    override fun createReview(review: Review): Review {
        return try {
            val url = reviewServiceUrl
            log.debug("Will call the createReview API on URL: $url")

            checkNotNull(
                restTemplate.postForObject(url, review, Review::class.java)?.also {
                    log.debug("Created a review with id: ${it.reviewId}")
                }
            ) { "Problem when trying to create a review for product with id ${review.productId} at $url" }

        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun deleteReviews(productId: Int) {
        try {
            val url = "$reviewServiceUrl?productId=$productId"
            log.debug("Will call the deleteReviews API on URL: $url")

            restTemplate.delete(url)

        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    private val HttpClientErrorException.errorMessage: String
        get() = try {
            mapper.readValue(responseBodyAsString, HttpErrorInfo::class.java).message
                ?: responseBodyAsString
        } catch (ex: IOException) {
            message ?: responseBodyAsString
        }

    private fun handleHttpClientException(exception: HttpClientErrorException): RuntimeException {
        return when (exception.statusCode) {
            NOT_FOUND -> NotFoundException(exception.errorMessage)
            UNPROCESSABLE_ENTITY -> InvalidInputException(exception.errorMessage)

            else -> {
                log.warn("Got an unexpected HTTP error: ${exception.statusCode}, will rethrow it")
                log.warn("Error body: ${exception.responseBodyAsString}")
                throw exception
            }
        }
    }
}
