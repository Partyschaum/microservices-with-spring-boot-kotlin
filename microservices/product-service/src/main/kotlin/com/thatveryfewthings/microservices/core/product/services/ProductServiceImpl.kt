package com.thatveryfewthings.microservices.core.product.services

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.product.ProductService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.exceptions.NotFoundException
import com.thatveryfewthings.api.http.ServiceUtil
import com.thatveryfewthings.microservices.core.product.persistence.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductServiceImpl(
    private val repository: ProductRepository,
    private val mapper: ProductMapper,
    private val serviceUtil: ServiceUtil,
) : ProductService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getProduct(productId: Int): Product {
        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        val entity = repository.findByProductId(productId)
            ?: throw NotFoundException("No product found for productId: $productId")

        return mapper.entityToApi(entity).copy(
            serviceAddress = serviceUtil.serviceAddress
        )
    }

    override fun createProduct(product: Product): Product {
        return try {
            val productEntity = mapper.apiToEntity(product)
            val newProductEntity = repository.save(productEntity)
            log.debug("createProduct: entity created for productId; ${product.productId}")

            mapper.entityToApi(newProductEntity).copy(
                serviceAddress = serviceUtil.serviceAddress
            )
        } catch (ex: DuplicateKeyException) {
            throw InvalidInputException("Duplicate key, Product id: ${product.productId}")
        }
    }

    override fun deleteProduct(productId: Int) {
        log.debug("deleteProduct: tries to delete entity with productId: $productId")
        repository.findByProductId(productId)?.let {
            repository.delete(it)
        }
    }
}
