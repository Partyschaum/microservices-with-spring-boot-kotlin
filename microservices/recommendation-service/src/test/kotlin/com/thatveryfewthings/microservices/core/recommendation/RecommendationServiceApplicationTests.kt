package com.thatveryfewthings.microservices.core.recommendation

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.kotlin.core.publisher.toMono

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = ["spring.data.mongodb.port: 0"],
)
class RecommendationServiceApplicationTests(
    @Autowired
    private val client: WebTestClient,
    @Autowired
    private val repository: RecommendationRepository,
) {

    @BeforeEach
    fun clearDb() {
        repository.deleteAll()
    }

    @Test
    fun getRecommendationsByProductId() {
        // Given
        val productId = 1
        val recommendationIds = listOf(1, 2, 3)
        assertEquals(0, repository.findByProductId(productId).size)

        // When
        recommendationIds.forEach {
            postAndVerifyRecommendation(productId, it, HttpStatus.OK)
        }

        // Then
        assertEquals(recommendationIds.size, repository.findByProductId(productId).size)
        getAndVerifyRecommendations(productId, HttpStatus.OK) {
            jsonPath("$.length()").isEqualTo(recommendationIds.size)

            recommendationIds.forEachIndexed { index, id ->
                jsonPath("$[$index].productId").isEqualTo(productId)
                jsonPath("$[$index].recommendationId").isEqualTo(id)

            }
        }
    }

    @Test
    fun duplicateError() {
        // Given
        val productId = 1
        val recommendationId = 1
        assertEquals(0, repository.count())

        // When
        postAndVerifyRecommendation(productId, recommendationId, HttpStatus.OK) {
            jsonPath("$.productId").isEqualTo(productId)
            jsonPath("$.recommendationId").isEqualTo(recommendationId)
        }

        // Then
        assertEquals(1, repository.count())
        postAndVerifyRecommendation(productId, recommendationId, HttpStatus.UNPROCESSABLE_ENTITY) {
            jsonPath("$.path").isEqualTo("/recommendation")
            jsonPath("$.message").isEqualTo("Duplicate key, Product id: $productId, Recommendation id: $recommendationId")
        }
        assertEquals(1, repository.count())
    }

    @Test
    fun deleteRecommendations() {
        // Given
        val productId = 1
        val recommendationId = 1

        postAndVerifyRecommendation(productId, recommendationId, HttpStatus.OK)
        assertEquals(1, repository.findByProductId(productId).size)

        // When
        deleteAndVerifyRecommendations(productId, HttpStatus.OK)

        // Then
        assertEquals(0, repository.findByProductId(productId).size)
        deleteAndVerifyRecommendations(productId, HttpStatus.OK)
    }

    @Test
    fun getRecommendationsMissingParameter() {
        // Given
        val missingProductIdQuery = ""

        // When
        getAndVerifyRecommendations(missingProductIdQuery, HttpStatus.BAD_REQUEST) {

            // Then
            jsonPath("$.path").isEqualTo("/recommendation")
            jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present")
        }
    }

    @Test
    fun getRecommendationsInvalidParameter() {
        // Given
        val invalidProductIdQuery = "?productId=no-integer"

        // When
        getAndVerifyRecommendations(invalidProductIdQuery, HttpStatus.BAD_REQUEST) {

            // Then
            jsonPath("$.path").isEqualTo("/recommendation")
            jsonPath("$.message").isEqualTo("Type mismatch.")
        }
    }

    @Test
    fun getRecommendationsNotFound() {
        // Given
        val productIdWithNonExistentRecommendations = 113

        // When
        getAndVerifyRecommendations(productIdWithNonExistentRecommendations, HttpStatus.OK) {

            // Then
            jsonPath("$.length()").isEqualTo(0)
        }
    }

    @Test
    fun getRecommendationsInvalidParameterNegativeValue() {
        // Given
        val invalidProductId = -1

        // When
        getAndVerifyRecommendations(invalidProductId, HttpStatus.UNPROCESSABLE_ENTITY) {

            // Then
            jsonPath("$.path").isEqualTo("/recommendation")
            jsonPath("$.message").isEqualTo("Invalid productId: $invalidProductId")
        }
    }

    private fun getAndVerifyRecommendations(
        productId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit = {},
    ) {
        getAndVerifyRecommendations("?productId=$productId", expectedStatus, bodyAssertions)
    }

    private fun getAndVerifyRecommendations(
        query: String,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit,
    ) {
        val bodyContentSpec = client.get()
            .uri("/recommendation$query")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }

    private fun postAndVerifyRecommendation(
        productId: Int,
        recommendationId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit = {},
    ) {
        val recommendation = Recommendation(
            productId = productId,
            recommendationId = recommendationId,
            author = "Author $recommendationId",
            rate = recommendationId,
            content = "Content $recommendationId",
            serviceAddress = "some service address",
        )

        val bodyContentSpec = client.post()
            .uri("/recommendation")
            .body(recommendation.toMono(), Product::class.java)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }

    private fun deleteAndVerifyRecommendations(
        productId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit = {}
    ) {
        val bodyContentSpec = client.delete()
            .uri("/recommendation?productId=$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }
}
