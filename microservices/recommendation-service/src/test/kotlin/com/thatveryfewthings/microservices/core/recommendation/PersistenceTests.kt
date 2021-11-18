package com.thatveryfewthings.microservices.core.recommendation

import com.thatveryfewthings.MongoDbTest
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationEntity
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.junit.jupiter.Testcontainers

@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
@Testcontainers
class PersistenceTests(
    @Autowired
    private val repository: RecommendationRepository
) : MongoDbTest() {

    @BeforeEach
    fun clearTable() {
        repository.deleteAll()
    }

    @Test
    fun create() {
        // Given
        val newEntity = RecommendationEntity(
            productId = 1,
            recommendationId = 2,
            author = "Me",
            rating = 3,
            content = "some content"
        )
        repository.save(newEntity)

        // When
        val persistedEntity = repository.findByIdOrNull(newEntity.id!!)!!

        // Then
        assertEqualsRecommendation(newEntity, persistedEntity)
        assertEquals(1, repository.count())
    }

    @Test
    fun update() {
        // Given
        val persistedEntity = aPersistedRecommendationEntity(
            productId = 2,
            recommendationId = 3,
            author = "Me",
            rating = 4,
            content = "some content"
        )

        // When
        persistedEntity.rating = 10
        repository.save(persistedEntity)

        // Then
        assertEquals(1, persistedEntity.version)
        assertEquals(10, persistedEntity.rating)
    }

    @Test
    fun delete() {
        // Given
        val persistedEntity = aPersistedRecommendationEntity(
            productId = 3,
            recommendationId = 4,
            author = "Me",
            rating = 5,
            content = "some content"
        )

        // When
        repository.delete(persistedEntity)

        // Then
        assertFalse(repository.existsById(persistedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        // Given
        val persistedEntity = aPersistedRecommendationEntity(
            productId = 4,
            recommendationId = 5,
            author = "Me",
            rating = 6,
            content = "some content"
        )

        // When
        val foundEntity = repository.findByProductId(persistedEntity.productId)

        // Then
        assertEquals(1, foundEntity.size)
        assertEqualsRecommendation(persistedEntity, foundEntity.first())
    }

    @Test
    fun duplicateError() {
        // Given
        aPersistedRecommendationEntity(
            productId = 5,
            recommendationId = 6,
            author = "Me",
            rating = 7,
            content = "some content"
        )

        // When
        val newEntity = RecommendationEntity(
            productId = 5,
            recommendationId = 6,
            author = "Me",
            rating = 7,
            content = "some content"
        )

        // Then
        assertThrows<DuplicateKeyException> {
            repository.save(newEntity)
        }
    }

    @Test
    fun optimisticLockingError() {
        // Given
        val persistedEntity = aPersistedRecommendationEntity(
            productId = 7,
            recommendationId = 8,
            author = "Me",
            rating = 9,
            content = "some content"
        )

        val foundEntity1 = repository.findByIdOrNull(persistedEntity.id!!)!!
        val foundEntity2 = repository.findByIdOrNull(persistedEntity.id!!)!!

        // When
        foundEntity1.content = "some different content"
        repository.save(foundEntity1)

        // Then
        assertThrows<OptimisticLockingFailureException> {
            foundEntity2.content = "some completely different content"
            repository.save(foundEntity2)
        }

        val updatedEntity = repository.findByIdOrNull(persistedEntity.id!!)!!
        assertEquals(1, updatedEntity.version)
        assertEquals("some different content", updatedEntity.content)
    }

    private fun aPersistedRecommendationEntity(
        productId: Int,
        recommendationId: Int,
        author: String,
        rating: Int,
        content: String,
    ): RecommendationEntity {
        val newEntity = RecommendationEntity(
            productId = productId,
            recommendationId = recommendationId,
            author = author,
            rating = rating,
            content = content,
        )
        repository.save(newEntity)
        return repository.findByIdOrNull(newEntity.id!!)!!
    }

    private fun assertEqualsRecommendation(expectedEntity: RecommendationEntity, actualEntity: RecommendationEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.recommendationId, actualEntity.recommendationId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.rating, actualEntity.rating)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}
