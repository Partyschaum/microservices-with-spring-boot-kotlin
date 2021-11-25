package com.thatveryfewthings.microservices.core.product.services

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.product.ProductService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.exceptions.NotFoundException
import com.thatveryfewthings.api.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductServiceImpl(
    private val serviceUtil: ServiceUtil
) : ProductService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getProduct(productId: Int): Product {
        log.debug("/product return the found product for productId=$productId")

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        if (productId == 13) {
            throw NotFoundException("No product found for productId: $productId")
        }

        return Product(
            productId = productId,
            name = "name-$productId",
            weight = 123,
            serviceAddress = serviceUtil.serviceAddress
        )
    }
}
