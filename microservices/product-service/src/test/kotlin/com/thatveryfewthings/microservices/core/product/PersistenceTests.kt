package com.thatveryfewthings.microservices.core.product

import com.mongodb.assertions.Assertions.assertFalse
import com.mongodb.assertions.Assertions.assertTrue
import com.thatveryfewthings.MongoDbTest
import com.thatveryfewthings.microservices.core.product.persistence.ProductEntity
import com.thatveryfewthings.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.junit.jupiter.Testcontainers

@DataMongoTest(excludeAutoConfiguration = [EmbeddedMongoAutoConfiguration::class])
@Testcontainers
class PersistenceTests(
    @Autowired
    private val repository: ProductRepository
) : MongoDbTest() {

    @BeforeEach
    fun clearTable() {
        repository.deleteAll()
    }

    @Test
    fun create() {
        // Given
        val newEntity = ProductEntity(productId = 1, name = "some name", weight = 2)
        repository.save(newEntity)

        // When
        val persistedEntity = repository.findByIdOrNull(newEntity.id)!!

        // Then
        assertEqualsProduct(newEntity, persistedEntity)
        assertEquals(1, repository.count())
    }

    @Test
    fun update() {
        // Given
        val persistedEntity = aPersistedProductEntity(productId = 2, name = "some name", weight = 3)

        // When
        persistedEntity.name = "some other name"
        repository.save(persistedEntity)

        // Then
        assertEquals(1, persistedEntity.version)
        assertEquals("some other name", persistedEntity.name)
    }

    @Test
    fun delete() {
        // Given
        val persistedEntity = aPersistedProductEntity(productId = 3, name = "some name", weight = 4)

        // When
        repository.delete(persistedEntity)

        // Then
        assertFalse(repository.existsById(persistedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        // Given
        val persistedEntity = aPersistedProductEntity(productId = 4, name = "some different name", weight = 5)

        // When
        val foundEntity = repository.findByProductId(persistedEntity.productId)

        // Then
        assertNotNull(foundEntity)
        assertEqualsProduct(persistedEntity, foundEntity!!)
    }

    @Test
    fun duplicateError() {
        // Given
        val persistedEntity = aPersistedProductEntity(productId = 5, name = "some name", weight = 6)

        // When
        val newEntity = ProductEntity(
            productId = persistedEntity.productId,
            name = "some different name",
            weight = 7
        )

        // Then
        assertThrows<DuplicateKeyException> {
            repository.save(newEntity)
        }
    }

    @Test
    fun optimisticLockingError() {
        // Given
        val persistedEntity = aPersistedProductEntity(productId = 1, name = "some name", weight = 2)

        val foundEntity1 = repository.findByIdOrNull(persistedEntity.id)!!
        val foundEntity2 = repository.findByIdOrNull(persistedEntity.id)!!

        // When
        foundEntity1.name = "some new name"
        repository.save(foundEntity1)

        // Then
        assertThrows<OptimisticLockingFailureException> {
            foundEntity2.name = "some completely new name"
            repository.save(foundEntity2)
        }

        val updatedEntity = repository.findByIdOrNull(persistedEntity.id)!!
        assertEquals(1, updatedEntity.version)
        assertEquals("some new name", updatedEntity.name)
    }

    @Test
    fun paging() {
        // Given
        val newProducts = (1001..1010).map {
            ProductEntity(it, "product $it", it)
        }
        repository.saveAll(newProducts)

        // When
        var nextPage: Pageable = PageRequest.of(0, 4, ASC, "productId")

        // Then
        nextPage = assertNextPage(nextPage) { page ->
            assertEquals(listOf(1001, 1002, 1003, 1004), page.content.map { it.productId })
            assertTrue(page.hasNext())
        }

        nextPage = assertNextPage(nextPage) { page ->
            assertEquals(listOf(1005, 1006, 1007, 1008), page.content.map { it.productId })
            assertTrue(page.hasNext())
        }

        assertNextPage(nextPage) { page ->
            assertEquals(listOf(1009, 1010), page.content.map { it.productId })
            assertFalse(page.hasNext())
        }
    }

    private fun aPersistedProductEntity(productId: Int, name: String, weight: Int): ProductEntity {
        val newEntity = ProductEntity(
            productId = productId,
            name = name,
            weight = weight,
        )
        repository.save(newEntity)
        return repository.findByIdOrNull(newEntity.id)!!
    }

    private fun assertEqualsProduct(expectedEntity: ProductEntity, actualEntity: ProductEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.name, actualEntity.name)
        assertEquals(expectedEntity.weight, actualEntity.weight)
    }

    private fun assertNextPage(
        nextPage: Pageable,
        assertPage: (it: Page<ProductEntity>) -> Unit
    ): Pageable {
        val productPage = repository.findAll(nextPage)

        assertPage(productPage)

        return productPage.nextPageable()
    }
}
