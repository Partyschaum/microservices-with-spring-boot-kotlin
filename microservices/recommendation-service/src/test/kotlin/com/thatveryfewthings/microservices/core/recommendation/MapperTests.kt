package com.thatveryfewthings.microservices.core.recommendation

import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.microservices.core.recommendation.services.RecommendationMapper
import com.thatveryfewthings.microservices.core.recommendation.services.RecommendationMapperImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [RecommendationMapperImpl::class],
)
class MapperTests(
    @Autowired
    private val mapper: RecommendationMapper,
) {

    @Test
    fun mapperTests() {
        // Given
        val recommendation = Recommendation(
            productId = 1,
            recommendationId = 2,
            author = "Me",
            rate = 8,
            content = "some content",
            serviceAddress = "some service address",
        )

        // When
        val recommendationEntity = mapper.apiToEntity(recommendation)

        // Then
        assertEquals(recommendation.productId, recommendationEntity.productId)
        assertEquals(recommendation.recommendationId, recommendationEntity.recommendationId)
        assertEquals(recommendation.author, recommendationEntity.author)
        assertEquals(recommendation.rate, recommendationEntity.rating)
        assertEquals(recommendation.content, recommendationEntity.content)

        // When
        val recommendation2 = mapper.entityToApi(recommendationEntity)

        // Then
        assertEquals(recommendation.productId, recommendation2.productId)
        assertEquals(recommendation.recommendationId, recommendation2.recommendationId)
        assertEquals(recommendation.author, recommendation2.author)
        assertEquals(recommendation.rate, recommendation2.rate)
        assertEquals(recommendation.content, recommendation2.content)
        assertNull(recommendation2.serviceAddress)
    }

    @Test
    fun mapperListTests() {
        // Given
        val recommendation = Recommendation(
            productId = 1,
            recommendationId = 2,
            author = "Me",
            rate = 8,
            content = "some content",
            serviceAddress = null,
        )
        val recommendations = listOf(recommendation)

        // When
        val recommendationEntities = mapper.apiListToEntityList(recommendations)

        // Then
        assertEquals(recommendations.size, recommendationEntities.size)

        val recommendationEntity = recommendationEntities.first()

        assertEquals(recommendation.productId, recommendationEntity.productId)
        assertEquals(recommendation.recommendationId, recommendationEntity.recommendationId)
        assertEquals(recommendation.author, recommendationEntity.author)
        assertEquals(recommendation.rate, recommendationEntity.rating)
        assertEquals(recommendation.content, recommendationEntity.content)

        // When
        val recommendations2 = mapper.entityListToApiList(recommendationEntities)

        // Then
        assertEquals(recommendations.size, recommendations2.size)

        val recommendation2 = recommendations2.first()

        assertEquals(recommendation.productId, recommendation2.productId)
        assertEquals(recommendation.recommendationId, recommendation2.recommendationId)
        assertEquals(recommendation.author, recommendation2.author)
        assertEquals(recommendation.rate, recommendation2.rate)
        assertEquals(recommendation.content, recommendation2.content)
        assertNull(recommendation2.serviceAddress)
    }
}
