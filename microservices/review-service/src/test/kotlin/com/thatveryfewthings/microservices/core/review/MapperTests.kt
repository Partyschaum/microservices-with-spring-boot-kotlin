package com.thatveryfewthings.microservices.core.review

import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.microservices.core.review.services.ReviewMapper
import com.thatveryfewthings.microservices.core.review.services.ReviewMapperImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [ReviewMapperImpl::class]
)
class MapperTests(
    @Autowired
    private val mapper: ReviewMapper
) {

    @Test
    fun mapperTests() {
        // Given
        val review = Review(
            productId = 1,
            reviewId = 2,
            author = "Me",
            subject = "some subject",
            content = "some content",
            serviceAddress = "some service address",
        )

        // When
        val reviewEntity = mapper.apiToEntity(review)

        // Then
        assertEquals(review.productId, reviewEntity.productId)
        assertEquals(review.reviewId, reviewEntity.reviewId)
        assertEquals(review.author, reviewEntity.author)
        assertEquals(review.subject, reviewEntity.subject)
        assertEquals(review.content, reviewEntity.content)

        // When
        val review2 = mapper.entityToApi(reviewEntity)

        // Then
        assertEquals(review.productId, review2.productId)
        assertEquals(review.reviewId, review2.reviewId)
        assertEquals(review.author, review2.author)
        assertEquals(review.subject, review2.subject)
        assertEquals(review.content, review2.content)
        assertNull(review2.serviceAddress)
    }

    @Test
    fun mapperListTests() {
        // Given
        val review = Review(
            productId = 1,
            reviewId = 2,
            author = "Me",
            subject = "some subject",
            content = "some content",
            serviceAddress = "some service address",
        )
        val reviews = listOf(review)

        // When
        val reviewEntities = mapper.apiListToEntityList(reviews)

        // Then
        assertEquals(reviews.size, reviewEntities.size)

        val reviewEntity = reviewEntities.first()

        assertEquals(review.productId, reviewEntity.productId)
        assertEquals(review.reviewId, reviewEntity.reviewId)
        assertEquals(review.author, reviewEntity.author)
        assertEquals(review.subject, reviewEntity.subject)
        assertEquals(review.content, reviewEntity.content)

        // When
        val reviews2 = mapper.entityListToApiList(reviewEntities)

        // Then
        assertEquals(reviews.size, reviews2.size)

        val review2 = reviews2.first()

        assertEquals(review.productId, review2.productId)
        assertEquals(review.reviewId, review2.reviewId)
        assertEquals(review.author, review2.author)
        assertEquals(review.subject, review2.subject)
        assertEquals(review.content, review2.content)
        assertNull(review2.serviceAddress)
    }
}
