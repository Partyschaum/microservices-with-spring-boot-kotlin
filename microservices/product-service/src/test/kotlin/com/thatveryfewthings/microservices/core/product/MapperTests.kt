package com.thatveryfewthings.microservices.core.product

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.microservices.core.product.services.ProductMapper
import com.thatveryfewthings.microservices.core.product.services.ProductMapperImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [ProductMapperImpl::class],
)
class MapperTests(
    @Autowired
    private val mapper: ProductMapper,
) {

    @Test
    fun mapperTests() {
        // Given
        val product = Product(
            productId = 1,
            name = "some name",
            weight = 2,
            serviceAddress = "some service address",
        )

        // When
        val productEntity = mapper.apiToEntity(product)

        // Then
        assertEquals(product.productId, productEntity.productId)
        assertEquals(product.name, productEntity.name)
        assertEquals(product.weight, productEntity.weight)

        // When
        val product2 = mapper.entityToApi(productEntity)

        // Then
        assertEquals(product.productId, product2.productId)
        assertEquals(product.name, product2.name)
        assertEquals(product.weight, product2.weight)
        assertNull(product2.serviceAddress)
    }
}
