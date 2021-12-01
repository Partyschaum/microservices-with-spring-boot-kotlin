package com.thatveryfewthings.microservices.core.product

import com.thatveryfewthings.MongoDbTest
import com.thatveryfewthings.microservices.core.product.persistence.ProductEntity
import com.thatveryfewthings.microservices.core.product.persistence.ProductRepository
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
    private val repository: ProductRepository,
) : MongoDbTest() {

    @BeforeEach
    fun clearTable() {
        repository.deleteAll().block()
    }

    @Test
    fun create() {
        val newEntity = ProductEntity(productId = 1, name = "some name", weight = 2)
        StepVerifier.create(repository.save(newEntity))
            .expectNextMatches { newEntity.productId == it.productId }
            .verifyComplete()

        StepVerifier.create(repository.findById(newEntity.id!!))
            .expectProductsAreEqual(newEntity)
            .verifyComplete()

        StepVerifier.create(repository.count())
            .expectNext(1)
            .verifyComplete()
    }

    @Test
    fun update() {
        withPersistedProductEntity(productId = 2, name = "some name", weight = 3) { persistedEntity ->

            persistedEntity.name = "some other name"

            StepVerifier.create(repository.save(persistedEntity))
                .expectNextMatches { it.name == "some other name" }
                .verifyComplete()

            StepVerifier.create(repository.findById(persistedEntity.id!!))
                .expectNextMatches { it.version == 1 && it.name == "some other name" }
                .verifyComplete()
        }
    }

    @Test
    fun delete() {
        withPersistedProductEntity(productId = 3, name = "some name", weight = 4) { persistedEntity ->

            StepVerifier.create(repository.delete(persistedEntity))
                .verifyComplete()

            StepVerifier.create(repository.existsById(persistedEntity.id!!))
                .expectNext(false)
                .verifyComplete()
        }
    }

    @Test
    fun getByProductId() {
        withPersistedProductEntity(productId = 4, name = "some different name", weight = 5) { persistedEntity ->
            StepVerifier.create(repository.findByProductId(persistedEntity.productId))
                .expectProductsAreEqual(persistedEntity)
                .verifyComplete()
        }
    }

    @Test
    fun duplicateError() {
        withPersistedProductEntity(productId = 5, name = "some name", weight = 6) { persistedEntity ->

            val newEntity = ProductEntity(
                productId = persistedEntity.productId,
                name = "some different name",
                weight = 7
            )

            StepVerifier.create(repository.save(newEntity))
                .expectError(DuplicateKeyException::class.java)
                .verify()
        }
    }

    @Test
    fun optimisticLockingError() {
        withPersistedProductEntity(productId = 1, name = "some name", weight = 2) { persistedEntity ->

            val foundEntity1 = repository.findById(persistedEntity.id!!).block()!!
            val foundEntity2 = repository.findById(persistedEntity.id!!).block()!!

            foundEntity1.name = "some new name"
            repository.save(foundEntity1).block()

            StepVerifier.create(repository.save(foundEntity2))
                .expectError(OptimisticLockingFailureException::class.java)
                .verify()

            StepVerifier.create(repository.findById(persistedEntity.id!!))
                .expectNextMatches {
                    it.version == 1 && it.name == "some new name"
                }
                .verifyComplete()

        }
    }

    private fun withPersistedProductEntity(
        productId: Int,
        name: String,
        weight: Int,
        onEntity: (entity: ProductEntity) -> Unit,
    ) {
        val newEntity = ProductEntity(
            productId = productId,
            name = name,
            weight = weight,
        )
        return repository.save(newEntity)
            .doOnNext { repository.findById(it.id!!) }
            .subscribe { onEntity(it) }
            .dispose()
    }

    private fun areProductsEqual(expectedEntity: ProductEntity, actualEntity: ProductEntity): Boolean {
        return expectedEntity.id == actualEntity.id &&
                expectedEntity.version == actualEntity.version &&
                expectedEntity.productId == actualEntity.productId &&
                expectedEntity.name == actualEntity.name &&
                expectedEntity.weight == actualEntity.weight
    }

    private fun StepVerifier.Step<ProductEntity>.expectProductsAreEqual(expectedEntity: ProductEntity): StepVerifier.Step<ProductEntity> {
        return expectNextMatches { areProductsEqual(expectedEntity, it) }
    }
}
