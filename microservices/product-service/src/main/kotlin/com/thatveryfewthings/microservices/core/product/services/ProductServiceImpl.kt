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
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.error

@RestController
class ProductServiceImpl(
    private val repository: ProductRepository,
    private val mapper: ProductMapper,
    private val serviceUtil: ServiceUtil,
) : ProductService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getProduct(productId: Int): Mono<Product> {
        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        return repository.findByProductId(productId)
            .switchIfEmpty(error(NotFoundException("No product found for productId: $productId")))
            .log()
            .map(mapper::entityToApi)
            .map { it.addServiceAddress() }
    }

    override fun createProduct(product: Product): Mono<Product> {
        if (product.productId < 1) {
            throw InvalidInputException("Invalid productId: ${product.productId}")
        }

        val productEntity = mapper.apiToEntity(product)

        return repository.save(productEntity)
            .log()
            .onErrorMap(DuplicateKeyException::class.java) {
                InvalidInputException("Duplicate key, Product id: ${product.productId}")
            }
            .map(mapper::entityToApi)
    }

    override fun deleteProduct(productId: Int): Mono<Void> {
        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        log.debug("deleteProduct: tries to delete entity with productId: $productId")
        return repository.findByProductId(productId)
            .log()
            .map { repository.delete(it) }
            .flatMap { it }
    }

    private fun Product.addServiceAddress() = copy(
        serviceAddress = serviceUtil.serviceAddress
    )
}
