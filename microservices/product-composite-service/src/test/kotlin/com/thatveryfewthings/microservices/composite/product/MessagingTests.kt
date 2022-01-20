package com.thatveryfewthings.microservices.composite.product

import com.thatveryfewthings.api.composite.product.ProductAggregate
import com.thatveryfewthings.api.composite.product.RecommendationSummary
import com.thatveryfewthings.api.composite.product.ReviewSummary
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.api.event.Event.Type.DELETE
import com.thatveryfewthings.microservices.composite.product.EventAssert.Companion.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.messaging.Message
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.kotlin.core.publisher.toMono

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [TestSecurityConfig::class],
    properties = [
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
        "spring.main.allow-bean-definition-overriding=true",
        "eureka.client.enabled = false",
    ],
)
@Import(TestChannelBinderConfiguration::class)
class MessagingTests(
    @Autowired
    private val target: OutputDestination,
    @Autowired
    private val client: WebTestClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @BeforeEach
    fun setUp() {
        purgeMessages("products")
        purgeMessages("recommendations")
        purgeMessages("reviews")
    }

    @Test
    fun createCompositeProduct1() {
        val compositeProduct = ProductAggregate(
            productId = 1,
            name = "some name",
            weight = 1,
            recommendations = emptyList(),
            reviews = emptyList(),
            serviceAddresses = null
        )

        postAndVerifyProduct(compositeProduct, ACCEPTED)

        val productMessages = getMessages("products")
        val recommendationMessages = getMessages("recommendations")
        val reviewMessages = getMessages("reviews")

        assertEquals(1, productMessages.size)
        assertEquals(0, recommendationMessages.size)
        assertEquals(0, reviewMessages.size)

        val expectedProductEvent = Event(
            eventType = Event.Type.CREATE,
            key = compositeProduct.productId,
            data = Product(
                productId = compositeProduct.productId,
                name = compositeProduct.name,
                weight = compositeProduct.weight,
                serviceAddress = null
            )
        )

        assertThat(productMessages.first()).isSameEvent(expectedProductEvent)
    }

    @Test
    fun createCompositeProduct2() {
        val recommendationSummary = RecommendationSummary(
            recommendationId = 1,
            author = "some author",
            rate = 1,
            content = "some content",
        )

        val reviewSummary = ReviewSummary(
            reviewId = 1,
            author = "some author",
            subject = "some subject",
            content = "some content",
        )

        val compositeProduct = ProductAggregate(
            productId = 1,
            name = "some name",
            weight = 1,
            recommendations = listOf(recommendationSummary),
            reviews = listOf(reviewSummary),
            serviceAddresses = null,
        )

        postAndVerifyProduct(compositeProduct, ACCEPTED)

        val productMessages = getMessages("products")
        val recommendationMessages = getMessages("recommendations")
        val reviewMessages = getMessages("reviews")

        assertEquals(1, productMessages.size)
        assertEquals(1, recommendationMessages.size)
        assertEquals(1, reviewMessages.size)

        val expectedProductEvent = Event(
            eventType = Event.Type.CREATE,
            key = compositeProduct.productId,
            data = Product(
                productId = compositeProduct.productId,
                name = compositeProduct.name,
                weight = compositeProduct.weight,
                serviceAddress = null
            )
        )

        val expectedRecommendationEvent = Event(
            eventType = Event.Type.CREATE,
            key = compositeProduct.productId,
            data = Recommendation(
                productId = compositeProduct.productId,
                recommendationId = recommendationSummary.recommendationId,
                author = recommendationSummary.author,
                rate = recommendationSummary.rate,
                content = recommendationSummary.content,
                serviceAddress = null,
            )
        )

        val expectedReviewEvent = Event(
            eventType = Event.Type.CREATE,
            key = compositeProduct.productId,
            data = Review(
                productId = compositeProduct.productId,
                reviewId = reviewSummary.reviewId,
                author = reviewSummary.author,
                subject = reviewSummary.subject,
                content = reviewSummary.content,
                serviceAddress = null,
            )
        )

        assertThat(productMessages.first()).isSameEvent(expectedProductEvent)
        assertThat(recommendationMessages.first()).isSameEvent(expectedRecommendationEvent)
        assertThat(reviewMessages.first()).isSameEvent(expectedReviewEvent)
    }

    @Test
    fun deleteCompositeProduct() {
        deleteAndVerifyProduct(1, ACCEPTED)

        val productMessages = getMessages("products")
        val recommendationMessages = getMessages("recommendations")
        val reviewMessages = getMessages("reviews")

        assertEquals(1, productMessages.size)
        assertEquals(1, recommendationMessages.size)
        assertEquals(1, reviewMessages.size)

        val expectedProductEvent = Event(
            eventType = DELETE,
            key = 1,
            data = null,
        )

        val expectedRecommendationEvent = Event(
            eventType = DELETE,
            key = 1,
            data = null,

            )

        val expectedReviewEvent = Event(
            eventType = DELETE,
            key = 1,
            data = null,
        )

        assertThat(productMessages.first()).isSameEvent(expectedProductEvent)
        assertThat(recommendationMessages.first()).isSameEvent(expectedRecommendationEvent)
        assertThat(reviewMessages.first()).isSameEvent(expectedReviewEvent)
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

    private fun purgeMessages(bindingName: String) {
        getMessage(bindingName)
    }

    private fun getMessages(bindingName: String): List<String> {
        val messages = mutableListOf<String>()
        var anyMoreMessages = true

        while (anyMoreMessages) {
            val message = getMessage(bindingName)
            if (message == null) {
                anyMoreMessages = false
            } else {
                messages += String(message.payload)
            }
        }

        return messages
    }

    private fun getMessage(bindingName: String): Message<ByteArray>? {
        return try {
            target.receive(0, bindingName)
        } catch (ex: NullPointerException) {
            log.error("getMessage() received a NPE with binding = $bindingName")
            null
        }
    }
}
