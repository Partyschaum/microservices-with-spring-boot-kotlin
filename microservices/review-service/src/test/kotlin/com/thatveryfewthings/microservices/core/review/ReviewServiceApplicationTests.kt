package com.thatveryfewthings.microservices.core.review

import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.event.Event.Type.CREATE
import com.thatveryfewthings.api.event.Event.Type.DELETE
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.microservices.core.review.config.MessageProcessor
import com.thatveryfewthings.microservices.core.review.persistence.ReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        "spring.datasource.url=jdbc:h2:mem:review-db",
        "eureka.client.enabled = false",
    ],
)
class ReviewServiceApplicationTests(
    @Autowired
    private val client: WebTestClient,
    @Autowired
    private val repository: ReviewRepository,
    @Autowired
    private val messageProcessor: MessageProcessor,
) {

    @BeforeEach
    fun clearDb() {
        repository.deleteAll()
    }

    @Test
    fun getReviewsByProduct() {
        // Given
        val productId = 1
        val reviewIds = listOf(1, 2, 3)

        assertEquals(0, repository.findByProductId(productId).size)

        // When
        reviewIds.forEach {
            sendCreateReviewEvent(
                productId = productId,
                reviewId = it,
            )
        }

        // Then
        assertEquals(reviewIds.size, repository.findByProductId(productId).size)
        getAndVerifyReviews(productId, HttpStatus.OK) {
            jsonPath("$.length()").isEqualTo(reviewIds.size)

            reviewIds.forEachIndexed { index, id ->
                jsonPath("$[$index].productId").isEqualTo(productId)
                jsonPath("$[$index].reviewId").isEqualTo(id)
            }
        }
    }

    @Test
    fun duplicateError() {
        // Given
        val productId = 1
        val reviewId = 1
        assertEquals(0, repository.count())

        // When
        sendCreateReviewEvent(productId, reviewId)

        // Then
        assertEquals(1, repository.count())

        val exception = assertThrows<InvalidInputException> {
            // When
            sendCreateReviewEvent(productId, reviewId)
        }

        // Then
        assertEquals("Duplicate key, Product id: $productId, Review id: $reviewId", exception.message)
        assertEquals(1, repository.count())
    }

    @Test
    fun deleteReviews() {
        // Given
        val productId = 1
        val reviewId = 1

        sendCreateReviewEvent(productId, reviewId)
        assertEquals(1, repository.findByProductId(productId).size)

        // When
        sendDeleteReviewEvent(productId)

        // Then
        assertEquals(0, repository.findByProductId(productId).size)
        assertDoesNotThrow {
            sendDeleteReviewEvent(productId)
        }
    }

    @Test
    fun getReviewsMissingParameter() {
        // Given
        val missingProductIdQuery = ""

        // When
        getAndVerifyReviews(missingProductIdQuery, HttpStatus.BAD_REQUEST) {

            // Then
            jsonPath("$.path").isEqualTo("/review")
            jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present")
        }
    }

    @Test
    fun getReviewsInvalidParameter() {
        // Given
        val invalidProductIdQuery = "?productId=no-integer"

        // When
        getAndVerifyReviews(invalidProductIdQuery, HttpStatus.BAD_REQUEST) {

            // Then
            jsonPath("$.path").isEqualTo("/review")
            jsonPath("$.message").isEqualTo("Type mismatch.")
        }
    }

    @Test
    fun getReviewsNotFound() {
        // Given
        val productIdWithNonExistentReviews = 213

        // When
        getAndVerifyReviews(productIdWithNonExistentReviews, HttpStatus.OK) {

            // Then
            jsonPath("$.length()").isEqualTo(0)
        }
    }

    @Test
    fun getReviewsInvalidParameterNegativeValue() {
        // Given
        val invalidProductId = -1

        // When
        getAndVerifyReviews(invalidProductId, HttpStatus.UNPROCESSABLE_ENTITY) {

            // Then
            jsonPath("$.path").isEqualTo("/review")
            jsonPath("$.message").isEqualTo("Invalid productId: $invalidProductId")
        }
    }

    private fun getAndVerifyReviews(
        productId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit = {},
    ) {
        getAndVerifyReviews("?productId=$productId", expectedStatus, bodyAssertions)
    }

    private fun getAndVerifyReviews(
        query: String,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit,
    ) {
        val bodyContentSpec = client.get()
            .uri("/review$query")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }

    private fun sendCreateReviewEvent(productId: Int, reviewId: Int) {
        val review = Review(
            productId = productId,
            reviewId = reviewId,
            author = "Author $reviewId",
            subject = "Subject $reviewId",
            content = "Content $reviewId",
            serviceAddress = "some service address",
        )

        val event = Event<Int, Review?>(
            eventType = CREATE,
            key = productId,
            data = review,
        )

        messageProcessor.accept(event)
    }

    private fun sendDeleteReviewEvent(productId: Int) {
        val event = Event<Int, Review?>(
            eventType = DELETE,
            key = productId,
            data = null,
        )

        messageProcessor.accept(event)
    }
}
