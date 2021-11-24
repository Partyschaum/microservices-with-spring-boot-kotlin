package com.thatveryfewthings.microservices.core.review

import com.thatveryfewthings.MySqlTest
import com.thatveryfewthings.microservices.core.review.persistence.ReviewEntity
import com.thatveryfewthings.microservices.core.review.persistence.ReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers
class PersistenceTests(
    @Autowired
    private val repository: ReviewRepository,
) : MySqlTest() {

    @BeforeEach
    fun clearTable() {
        repository.deleteAll()
    }

    @Test
    fun create() {
        // Given
        val newEntity = ReviewEntity(
            productId = 1,
            reviewId = 2,
            author = "Me",
            subject = "nice entity",
            content = "a nice review",
        )
        repository.save(newEntity)

        // When
        val persistedEntity = repository.findByIdOrNull(newEntity.id!!)!!

        // Then
        assertEqualsReview(newEntity, persistedEntity)
        assertEquals(1, repository.count())
    }

    @Test
    fun update() {
        // Given
        val persistedEntity = aPersistedReviewEntity(
            productId = 2,
            reviewId = 3,
            author = "Me",
            subject = "nice entity",
            content = "a nice review",
        )

        // When
        persistedEntity.subject = "super entity"
        repository.save(persistedEntity)
        val foundEntity = repository.findByIdOrNull(persistedEntity.id!!)!!

        // Then
        assertEquals(1, foundEntity.version)
        assertEquals("super entity", foundEntity.subject)
    }

    @Test
    fun delete() {
        // Given
        val persistedEntity = aPersistedReviewEntity(
            productId = 3,
            reviewId = 4,
            author = "Me",
            subject = "nice entity",
            content = "a nice review",
        )

        // When
        repository.delete(persistedEntity)

        // Then
        assertFalse(repository.existsById(persistedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        // Given
        val persistedEntity = aPersistedReviewEntity(
            productId = 3,
            reviewId = 4,
            author = "Me",
            subject = "nice entity",
            content = "a nice review",
        )

        // When
        val foundEntities = repository.findByProductId(persistedEntity.productId)

        // Then
        assertEquals(1, foundEntities.size)
        assertEqualsReview(persistedEntity, foundEntities.first())
    }

    @Test
    fun duplicateError() {
        // Given
        aPersistedReviewEntity(
            productId = 4,
            reviewId = 5,
            author = "Me",
            subject = "nice entity",
            content = "a nice review",
        )

        // When
        val newEntity = ReviewEntity(
            productId = 4,
            reviewId = 5,
            author = "Me",
            subject = "nice entity",
            content = "a nice review",
        )

        // Then
        val exception = assertThrows<DataIntegrityViolationException> {
            repository.save(newEntity)
        }

        println("### ${exception.message}")
    }

    @Test
    fun optimisticLockingError() {
        // Given
        val persistedEntity = aPersistedReviewEntity(
            productId = 5,
            reviewId = 6,
            author = "Me",
            subject = "some subject",
            content = "a nice review",
        )

        val foundEntity1 = repository.findByIdOrNull(persistedEntity.id!!)!!
        val foundEntity2 = repository.findByIdOrNull(persistedEntity.id!!)!!

        // When
        foundEntity1.subject = "some different subject"
        repository.save(foundEntity1)

        // Then
        assertThrows<ObjectOptimisticLockingFailureException> {
            foundEntity2.subject = "some completely different subject"
            repository.save(foundEntity2)
        }

        val updatedEntity = repository.findByIdOrNull(persistedEntity.id!!)!!
        assertEquals(1, updatedEntity.version)
        assertEquals("some different subject", updatedEntity.subject)
    }

    private fun aPersistedReviewEntity(
        productId: Int,
        reviewId: Int,
        author: String,
        subject: String,
        content: String
    ): ReviewEntity {
        val newEntity = ReviewEntity(
            productId = productId,
            reviewId = reviewId,
            author = author,
            subject = subject,
            content = content,
        )
        repository.save(newEntity)
        return repository.findByIdOrNull(newEntity.id!!)!!
    }

    private fun assertEqualsReview(expectedEntity: ReviewEntity, actualEntity: ReviewEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.reviewId, actualEntity.reviewId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.subject, actualEntity.subject)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}
