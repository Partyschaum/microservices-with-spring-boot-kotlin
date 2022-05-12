package com.thatveryfewthings.microservices.core.recommendation

import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.event.Event.Type.CREATE
import com.thatveryfewthings.api.event.Event.Type.DELETE
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.microservices.core.recommendation.config.MessageProcessor
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        "spring.data.mongodb.port: 0",
        "eureka.client.enabled = false",
        "spring.cloud.stream.default.consumer.autoStartup: false",
    ],
)
class RecommendationServiceApplicationTests(
    @Autowired
    private val client: WebTestClient,
    @Autowired
    private val repository: RecommendationRepository,
    @Autowired
    private val messageProcessor: MessageProcessor,
) {

    @BeforeEach
    fun clearDb() {
        repository.deleteAll().block()
    }

    @Test
    fun getRecommendationsByProductId() {
        // Given
        val productId = 1
        val recommendationIds = listOf(1, 2, 3)

        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(0)
            .verifyComplete()

        // When
        recommendationIds.forEach {
            sendCreateRecommendationEvent(
                productId = productId,
                recommendationId = it,
            )
        }

        // Then
        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(recommendationIds.size.toLong())
            .verifyComplete()

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

        StepVerifier.create(repository.count())
            .expectNext(0)
            .verifyComplete()

        sendCreateRecommendationEvent(productId, recommendationId)

        StepVerifier.create(repository.count())
            .expectNext(1)
            .verifyComplete()

        val exception = assertThrows<InvalidInputException> {

            // When
            sendCreateRecommendationEvent(productId, recommendationId)
        }

        // Then
        assertEquals(
            "Duplicate key, Product id: $productId, Recommendation id: $recommendationId",
            exception.message
        )

        StepVerifier.create(repository.count())
            .expectNext(1)
            .verifyComplete()
    }

    @Test
    fun deleteRecommendations() {
        // Given
        val productId = 1
        val recommendationId = 1

        sendCreateRecommendationEvent(productId, recommendationId)

        StepVerifier.create(repository.count())
            .expectNext(1)
            .verifyComplete()

        // When
        sendDeleteRecommendationEvent(productId)

        // Then
        StepVerifier.create(repository.count())
            .expectNext(0)
            .verifyComplete()

        sendDeleteRecommendationEvent(productId)
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

    private fun sendCreateRecommendationEvent(productId: Int, recommendationId: Int) {
        val recommendation = Recommendation(
            productId = productId,
            recommendationId = recommendationId,
            author = "Author $recommendationId",
            rate = recommendationId,
            content = "Content $recommendationId",
            serviceAddress = "some service address",
        )

        val event = Event<Int, Recommendation?>(
            eventType = CREATE,
            key = productId,
            data = recommendation,
        )

        messageProcessor.accept(event)
    }

    private fun sendDeleteRecommendationEvent(productId: Int) {
        val event = Event<Int, Recommendation?>(
            eventType = DELETE,
            key = productId,
            data = null,
        )

        messageProcessor.accept(event)
    }
}
