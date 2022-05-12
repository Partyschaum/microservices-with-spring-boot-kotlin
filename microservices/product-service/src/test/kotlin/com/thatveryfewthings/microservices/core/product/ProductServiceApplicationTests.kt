package com.thatveryfewthings.microservices.core.product

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.event.Event.Type.CREATE
import com.thatveryfewthings.api.event.Event.Type.DELETE
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.microservices.core.product.config.MessageProcessor
import com.thatveryfewthings.microservices.core.product.persistence.ProductRepository
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
class ProductServiceApplicationTests(
    @Autowired
    private val client: WebTestClient,
    @Autowired
    private val repository: ProductRepository,
    @Autowired
    private val messageProcessor: MessageProcessor
) {

    @BeforeEach
    fun clearDb() {
        repository.deleteAll().block()
    }

    @Test
    fun getProductById() {
        // Given
        val productId = 1

        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(0)
            .verifyComplete()

        StepVerifier.create(repository.count())
            .expectNext(0)
            .verifyComplete()

        // When
        sendCreateProductEvent(productId)

        // Then
        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(1)
            .verifyComplete()

        StepVerifier.create(repository.count())
            .expectNext(1)
            .verifyComplete()

        getAndVerifyProduct(productId, HttpStatus.OK) {
            jsonPath("$.productId").isEqualTo(productId)
        }
    }

    @Test
    fun duplicateError() {
        // Given
        val productId = 1

        sendCreateProductEvent(productId)

        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(1)
            .verifyComplete()

        val exception = assertThrows<InvalidInputException>("Expected a InvalidInputException here!") {

            // When
            sendCreateProductEvent(productId)
        }

        // Then
        assertEquals("Duplicate key, Product id: $productId", exception.message)
    }

    @Test
    fun deleteProduct() {
        // Given
        val productId = 1

        sendCreateProductEvent(productId)

        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(1)
            .verifyComplete()

        // When
        sendDeleteProductEvent(productId)

        // Then
        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(0)
            .verifyComplete()

        sendDeleteProductEvent(productId)
    }

    @Test
    fun getProductInvalidParameterString() {
        // Given
        val invalidProductId = "no-integer"

        // When
        getAndVerifyProduct(invalidProductId, HttpStatus.BAD_REQUEST) {

            // Then
            jsonPath("$.path").isEqualTo(("/product/no-integer"))
            jsonPath("$.message").isEqualTo("Type mismatch.")
        }
    }

    @Test
    fun getProductNotFound() {
        // Given
        val nonExistentProductId = 13

        // When
        getAndVerifyProduct(nonExistentProductId, HttpStatus.NOT_FOUND) {

            // Then
            jsonPath("$.path").isEqualTo("/product/$nonExistentProductId")
            jsonPath("$.message").isEqualTo("No product found for productId: $nonExistentProductId")
        }
    }

    @Test
    fun getProductInvalidParameterNegativeValue() {
        // Given
        val invalidProductId = -1

        // When
        getAndVerifyProduct(invalidProductId, HttpStatus.UNPROCESSABLE_ENTITY) {

            // Then
            jsonPath("$.path").isEqualTo("/product/$invalidProductId")
            jsonPath("$.message").isEqualTo("Invalid productId: $invalidProductId")
        }
    }

    private fun getAndVerifyProduct(
        productId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit,
    ) {
        getAndVerifyProduct("$productId", expectedStatus, bodyAssertions)
    }

    private fun getAndVerifyProduct(
        path: String,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit,
    ) {
        val bodyContentSpec = client.get()
            .uri("/product/$path")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }

    private fun sendCreateProductEvent(productId: Int) {
        val product = Product(
            productId = productId,
            name = "Name $productId",
            weight = productId,
            serviceAddress = "some service address",
        )

        val event = Event<Int, Product?>(
            eventType = CREATE,
            key = productId,
            data = product,
        )

        messageProcessor.accept(event)
    }

    private fun sendDeleteProductEvent(productId: Int) {
        val event = Event<Int, Product?>(
            eventType = DELETE,
            key = productId,
            data = null,
        )

        messageProcessor.accept(event)
    }
}
