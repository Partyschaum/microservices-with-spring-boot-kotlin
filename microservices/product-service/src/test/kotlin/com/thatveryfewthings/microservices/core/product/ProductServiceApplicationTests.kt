package com.thatveryfewthings.microservices.core.product

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = ["spring.data.mongodb.port: 0"],
)
class ProductServiceApplicationTests(
    @Autowired
    private val client: WebTestClient,
    @Autowired
    private val repository: ProductRepository,
) {

    @BeforeEach
    fun clearDb() {
        repository.deleteAll().block()
    }

    @Test
    fun getProductById() {
        // Given
        val productId = 1
        postAndVerifyProduct(productId, HttpStatus.OK)

        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(1)
            .verifyComplete()

        // When
        getAndVerifyProduct(productId, HttpStatus.OK) {

            // Then
            jsonPath("$.productId").isEqualTo(productId)
        }
    }

    @Test
    fun duplicateError() {
        // Given
        val productId = 1
        postAndVerifyProduct(productId, HttpStatus.OK)

        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(1)
            .verifyComplete()

        // When
        postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY) {

            // Then
            jsonPath("$.path").isEqualTo("/product")
            jsonPath("$.message").isEqualTo("Duplicate key, Product id: $productId")
        }
    }

    @Test
    fun deleteProduct() {
        // Given
        val productId = 1
        postAndVerifyProduct(productId, HttpStatus.OK)

        StepVerifier.create(repository.findByProductId(productId))
            .expectNextCount(1)
            .verifyComplete()

        // When
        deleteAndVerifyProduct(productId, HttpStatus.OK)

        // Then
        StepVerifier.create(repository.findByProductId(productId))
            .verifyComplete()

        deleteAndVerifyProduct(productId, HttpStatus.OK)
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

    private fun postAndVerifyProduct(
        productId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit = {},
    ) {
        val product = Product(
            productId = productId,
            name = "Name $productId",
            weight = productId,
            serviceAddress = "some service address",
        )

        val bodyContentSpec = client.post()
            .uri("/product")
            .body(product.toMono(), Product::class.java)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }

    private fun deleteAndVerifyProduct(
        productId: Int,
        expectedStatus: HttpStatus,
        bodyAssertions: WebTestClient.BodyContentSpec.() -> Unit = {}
    ) {
        val bodyContentSpec = client.delete()
            .uri("/product/$productId")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()

        bodyAssertions(bodyContentSpec)
    }
}
