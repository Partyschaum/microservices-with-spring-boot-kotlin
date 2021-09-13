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
import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import com.thatveryfewthings.util.http.HttpErrorInfo
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

    private val productServiceUrl = "${configuration.productService.url}/product/"
    private val recommendationServiceUrl = "${configuration.recommendationService.url}/recommendation?productId="
    private val reviewServiceUrl = "${configuration.reviewService.url}/review?productId="

    override fun getProduct(productId: Int): Product {
        return try {
            val url = productServiceUrl + productId

            log.debug("Will call getProduct API on URL: $url")

            restTemplate.getForObject(url, Product::class.java)?.also {
                log.debug("Found a product with id: $productId")
            } ?: throw NotFoundException("No product found for productId: $productId")

        } catch (ex: HttpClientErrorException) {

            when (ex.statusCode) {
                NOT_FOUND -> throw NotFoundException(ex.errorMessage)
                UNPROCESSABLE_ENTITY -> throw InvalidInputException(ex.errorMessage)

                else -> {
                    log.warn("Got an unexpected HTTP error: ${ex.statusCode}, will rethrow it")
                    log.warn("Error body: ${ex.responseBodyAsString}")
                    throw ex
                }
            }
        }
    }

    override fun getRecommendations(productId: Int): List<Recommendation> {
        return try {
            val url = recommendationServiceUrl + productId

            log.debug("Will call getRecommendations API on URL: $url")
            restTemplate.exchange(url, GET, null, object : ParameterizedTypeReference<List<Recommendation>>() {}).body
                ?: emptyList<Recommendation>().also {
                    log.warn("No recommendations found for productId: $productId")
                }
        } catch (ex: Exception) {
            log.warn("Got an exception while requesting recommendations, return zero recommendations: ${ex.message}")
            emptyList()
        }
    }

    override fun getReviews(productId: Int): List<Review> {
        return try {
            val url = reviewServiceUrl + productId

            log.debug("Will call getReviews API on URL: $url")
            restTemplate.exchange(url, GET, null, object : ParameterizedTypeReference<List<Review>>() {}).body
                ?: emptyList<Review>().also {
                    log.warn("No reviews found for productId: $productId")
                }
        } catch (ex: Exception) {
            log.warn("Got an exception while requesting reviews, return zero reviews: ${ex.message}")
            emptyList()
        }
    }

    private val HttpClientErrorException.errorMessage: String
        get() = try {
            mapper.readValue(this.responseBodyAsString, HttpErrorInfo::class.java).message
                ?: this.responseBodyAsString
        } catch (ex: IOException) {
            this.message
                ?: this.responseBodyAsString
        }
}
