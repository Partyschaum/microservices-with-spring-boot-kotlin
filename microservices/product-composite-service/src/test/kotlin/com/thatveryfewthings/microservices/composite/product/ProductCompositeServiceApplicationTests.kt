package com.thatveryfewthings.microservices.composite.product

import com.ninjasquad.springmockk.MockkBean
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
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests(
    @Autowired
    private val client: WebTestClient
) {

    private val productIdOk = 1
    private val productIdNotFound = 2
    private val productIdInvalid = 3

    @MockkBean
    private lateinit var compositeIntegration: ProductCompositeIntegration

    @BeforeEach
    fun setUp() {

        every { compositeIntegration.getProduct(productIdOk) } returns Product(
            productId = productIdOk,
            name = "name",
            weight = 1,
            serviceAddress = "mock-address"
        )

        every { compositeIntegration.getRecommendations(productIdOk) } returns listOf(
            Recommendation(
                productId = productIdOk,
                recommendationId = 1,
                author = "author",
                rate = 1,
                content = "content",
                serviceAddress = "mock-address"
            )
        )

        every { compositeIntegration.getReviews(productIdOk) } returns listOf(
            Review(
                productId = productIdOk,
                reviewId = 1,
                author = "author",
                subject = "subject",
                content = "content",
                serviceAddress = "mock-address"
            )
        )

        every { compositeIntegration.getProduct(productIdNotFound) } throws NotFoundException("NOT FOUND: $productIdNotFound")

        every { compositeIntegration.getProduct(productIdInvalid) } throws InvalidInputException("INVALID: $productIdInvalid")
    }

    @Test
    fun getProductById() {

        client.get()
            .uri("/product-composite/$productIdOk")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.productId").isEqualTo(productIdOk)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1)
    }

    @Test
    fun getProductNotFound() {

        client.get()
            .uri("/product-composite/$productIdNotFound")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/product-composite/$productIdNotFound")
            .jsonPath("$.message").isEqualTo("NOT FOUND: $productIdNotFound")
    }

    @Test
    fun getProductInvalidInput() {

        client.get()
            .uri("/product-composite/$productIdInvalid")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/product-composite/$productIdInvalid")
            .jsonPath("$.message").isEqualTo("INVALID: $productIdInvalid")
    }
}
