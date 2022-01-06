package com.thatveryfewthings.microservices.composite.product

import com.ninjasquad.springmockk.MockkBean
import com.thatveryfewthings.api.composite.product.ProductAggregate
import com.thatveryfewthings.api.composite.product.RecommendationSummary
import com.thatveryfewthings.api.composite.product.ReviewSummary
import com.thatveryfewthings.api.composite.product.ServiceAddresses
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.exceptions.NotFoundException
import com.thatveryfewthings.microservices.composite.product.services.ProductCompositeIntegration
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests(
    @Autowired
    private val client: WebTestClient,
) {

    private val productIdOk = 1
    private val productIdNotFound = 2
    private val productIdInvalid = 3

    @MockkBean
    private lateinit var compositeIntegration: ProductCompositeIntegration

    @BeforeEach
    fun setUp() {
        every { compositeIntegration.createProduct(any()) } returns Product(
            productId = 1,
            name = "name",
            weight = 1,
            serviceAddress = "mock-address",
        ).toMono()

        every { compositeIntegration.createRecommendation(any()) } returns Recommendation(
            productId = productIdOk,
            recommendationId = 1,
            author = "author",
            rate = 1,
            content = "content",
            serviceAddress = "mock-address",
        ).toMono()

        every { compositeIntegration.createReview(any()) } returns Review(
            productId = productIdOk,
            reviewId = 1,
            author = "author",
            subject = "subject",
            content = "content",
            serviceAddress = "mock-address",
        ).toMono()

        every { compositeIntegration.deleteProduct(any()) } returns Mono.empty()
        every { compositeIntegration.deleteRecommendations(any()) } returns Mono.empty()
        every { compositeIntegration.deleteReviews(any()) } returns Mono.empty()

        every { compositeIntegration.getProduct(productIdOk) } returns Product(
            productId = productIdOk,
            name = "name",
            weight = 1,
            serviceAddress = "mock-address",
        ).toMono()

        every { compositeIntegration.getRecommendations(productIdOk) } returns listOf(
            Recommendation(
                productId = productIdOk,
                recommendationId = 1,
                author = "author",
                rate = 1,
                content = "content",
                serviceAddress = "mock-address",
            )
        ).toFlux()

        every { compositeIntegration.getReviews(productIdOk) } returns listOf(
            Review(
                productId = productIdOk,
                reviewId = 1,
                author = "author",
                subject = "subject",
                content = "content",
                serviceAddress = "mock-address",
            )
        ).toFlux()

        every { compositeIntegration.getProduct(productIdNotFound) } throws NotFoundException("NOT FOUND: $productIdNotFound")

        every { compositeIntegration.getProduct(productIdInvalid) } throws InvalidInputException("INVALID: $productIdInvalid")
    }

    @Test
    fun createBasicCompositeProduct() {
        // Given
        val compositeProduct = ProductAggregate(
            productId = 1,
            name = "some product",
            weight = 2,
            recommendations = emptyList(),
            reviews = emptyList(),
            serviceAddresses = ServiceAddresses(
                compositeAddress = null,
                productAddress = null,
                reviewAddress = null,
                recommendationAddress = null,
            )
        )

        // When & Then
        postAndVerifyProduct(compositeProduct, ACCEPTED)
    }

    @Test
    fun createFullCompositeProduct() {
        // Given
        val compositeProduct = ProductAggregate(
            productId = 1,
            name = "some product",
            weight = 2,
            recommendations = listOf(
                RecommendationSummary(
                    recommendationId = 1,
                    author = "Me",
                    rate = 2,
                    content = "some content",
                )
            ),
            reviews = listOf(
                ReviewSummary(
                    reviewId = 1,
                    author = "Me",
                    subject = "some subject",
                    content = "some content",
                )
            ),
            serviceAddresses = ServiceAddresses(
                compositeAddress = null,
                productAddress = null,
                reviewAddress = null,
                recommendationAddress = null
            )
        )

        // When & Then
        postAndVerifyProduct(compositeProduct, ACCEPTED)
    }

    @Test
    fun deleteCompositeProduct() {
        // Given
        val compositeProduct = ProductAggregate(
            productId = 1,
            name = "some product",
            weight = 2,
            recommendations = listOf(
                RecommendationSummary(
                    recommendationId = 1,
                    author = "Me",
                    rate = 2,
                    content = "some content",
                )
            ),
            reviews = listOf(
                ReviewSummary(
                    reviewId = 1,
                    author = "Me",
                    subject = "some subject",
                    content = "some content",
                )
            ),
            serviceAddresses = ServiceAddresses(
                compositeAddress = null,
                productAddress = null,
                reviewAddress = null,
                recommendationAddress = null
            )
        )

        postAndVerifyProduct(compositeProduct, ACCEPTED)

        // When
        deleteAndVerifyProduct(compositeProduct.productId, ACCEPTED)
        deleteAndVerifyProduct(compositeProduct.productId, ACCEPTED)
    }

    @Test
    fun getProductById() {
        // Given
        val productId = productIdOk

        // When
        getAndVerifyProduct(productId, OK) {

            // Then
            jsonPath("$.productId").isEqualTo(productIdOk)
            jsonPath("$.recommendations.length()").isEqualTo(1)
            jsonPath("$.reviews.length()").isEqualTo(1)
        }
    }

    @Test
    fun getProductNotFound() {
        // Given
        val productId = productIdNotFound

        // When
        getAndVerifyProduct(productId, NOT_FOUND) {

            // Then
            jsonPath("$.path").isEqualTo("/product-composite/$productIdNotFound")
            jsonPath("$.message").isEqualTo("NOT FOUND: $productIdNotFound")
        }
    }

    @Test
    fun getProductInvalidInput() {
        // Given
        val productId = productIdInvalid

        // When
        getAndVerifyProduct(productId, UNPROCESSABLE_ENTITY) {

            // Then
            jsonPath("$.path").isEqualTo("/product-composite/$productIdInvalid")
            jsonPath("$.message").isEqualTo("INVALID: $productIdInvalid")
        }
    }

    private fun getAndVerifyProduct(
        productId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit = {},
    ) {
        val bodyContentSpec = client.get()
            .uri("/product-composite/$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }

    private fun postAndVerifyProduct(compositeProduct: ProductAggregate, expectedStatus: HttpStatus) {
        client.post()
            .uri("/product-composite")
            .body(compositeProduct.toMono(), ProductAggregate::class.java)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus) {
        client.delete()
            .uri("/product-composite/$productId")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }
}
