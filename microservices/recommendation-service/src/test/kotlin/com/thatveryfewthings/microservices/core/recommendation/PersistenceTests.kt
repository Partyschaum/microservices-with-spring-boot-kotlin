package com.thatveryfewthings.microservices.core.recommendation

import com.thatveryfewthings.MongoDbTest
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationEntity
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier

@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
@Testcontainers
class PersistenceTests(
    @Autowired
    private val repository: RecommendationRepository,
) : MongoDbTest() {

    @BeforeEach
    fun clearTable() {
        repository.deleteAll().block()
    }

    @Test
    fun create() {
        val newEntity = RecommendationEntity(
            productId = 1,
            recommendationId = 2,
            author = "Me",
            rating = 3,
            content = "some content"
        )
        StepVerifier.create(repository.save(newEntity))
            .expectRecommendationsAreEqual(newEntity)
            .verifyComplete()

        StepVerifier.create(repository.findById(newEntity.id!!))
            .expectRecommendationsAreEqual(newEntity)
            .verifyComplete()

        StepVerifier.create(repository.count())
            .expectNext(1)
            .verifyComplete()
    }

    @Test
    fun update() {
        withPersistedRecommendationEntity(
            productId = 2,
            recommendationId = 3,
            author = "Me",
            rating = 4,
            content = "some content"
        ) { persistedEntity ->

            persistedEntity.rating = 10

            StepVerifier.create(repository.save(persistedEntity))
                .expectNextMatches {
                    it.version == 1 && it.rating == 10
                }
                .verifyComplete()
        }
    }

    @Test
    fun delete() {
        withPersistedRecommendationEntity(
            productId = 3,
            recommendationId = 4,
            author = "Me",
            rating = 5,
            content = "some content"
        ) { persistedEntity ->

            StepVerifier.create(repository.delete(persistedEntity))
                .verifyComplete()

            StepVerifier.create(repository.existsById(persistedEntity.id!!))
                .expectNext(false)
                .verifyComplete()
        }
    }

    @Test
    fun getByProductId() {
        withPersistedRecommendationEntity(
            productId = 4,
            recommendationId = 5,
            author = "Me",
            rating = 6,
            content = "some content"
        ) { persistedEntity ->

            StepVerifier.create(repository.findByProductId(persistedEntity.productId).collectList())
                .expectNextMatches { it.size == 1 && areRecommendationsEqual(persistedEntity, it.first()) }
                .verifyComplete()
        }
    }

    @Test
    fun duplicateError() {
        withPersistedRecommendationEntity(
            productId = 5,
            recommendationId = 6,
            author = "Me",
            rating = 7,
            content = "some content"
        ) {

            val newEntity = RecommendationEntity(
                productId = 5,
                recommendationId = 6,
                author = "Me",
                rating = 7,
                content = "some content"
            )

            StepVerifier.create(repository.save(newEntity))
                .expectError(DuplicateKeyException::class.java)
                .verify()
        }
    }

    @Test
    fun optimisticLockingError() {
        withPersistedRecommendationEntity(
            productId = 7,
            recommendationId = 8,
            author = "Me",
            rating = 9,
            content = "some content"
        ) { persistedEntity ->

            val foundEntity1 = repository.findById(persistedEntity.id!!).block()!!
            val foundEntity2 = repository.findById(persistedEntity.id!!).block()!!

            foundEntity1.content = "some different content"
            repository.save(foundEntity1).block()

            StepVerifier.create(repository.save(foundEntity2))
                .expectError(OptimisticLockingFailureException::class.java)
                .verify()

            StepVerifier.create(repository.findById(persistedEntity.id!!))
                .expectNextMatches {
                    it.version == 1 && it.content == "some different content"
                }
        }
    }

    private fun withPersistedRecommendationEntity(
        productId: Int,
        recommendationId: Int,
        author: String,
        rating: Int,
        content: String,
        onEntity: (entity: RecommendationEntity) -> Unit,
    ) {
        val newEntity = RecommendationEntity(
            productId = productId,
            recommendationId = recommendationId,
            author = author,
            rating = rating,
            content = content,
        )
        return repository.save(newEntity)
            .doOnNext { repository.findById(newEntity.id!!) }
            .subscribe { onEntity(it) }
            .dispose()
    }

    private fun areRecommendationsEqual(
        expectedEntity: RecommendationEntity,
        actualEntity: RecommendationEntity
    ): Boolean {
        return expectedEntity.id == actualEntity.id &&
                expectedEntity.version == actualEntity.version &&
                expectedEntity.productId == actualEntity.productId &&
                expectedEntity.recommendationId == actualEntity.recommendationId &&
                expectedEntity.author == actualEntity.author &&
                expectedEntity.rating == actualEntity.rating &&
                expectedEntity.content == actualEntity.content
    }

    private fun StepVerifier.Step<RecommendationEntity>.expectRecommendationsAreEqual(expectedEntity: RecommendationEntity): StepVerifier.Step<RecommendationEntity> {
        return expectNextMatches { areRecommendationsEqual(expectedEntity, it) }
    }
}
