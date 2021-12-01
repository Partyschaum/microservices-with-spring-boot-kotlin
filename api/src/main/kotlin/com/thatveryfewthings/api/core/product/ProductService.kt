package com.thatveryfewthings.api.core.product

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

interface ProductService {

    /**
     * Sample usage:
     * curl $HOST:$PORT/product/1
     *
     * @param productId Id of the product
     * @return the product, if found, else null
     */
    @GetMapping(
        value = ["/product/{productId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getProduct(
        @PathVariable
        productId: Int,
    ): Mono<Product>

    /**
     * Sample usage:
     * curl -X POST $HOST:$PORT/product \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123, "name":"product 123", "weight":123}'
     *
     * @param product the JSON structure of the product
     * @return the created product
     */
    @PostMapping(
        value = ["/product"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createProduct(
        @RequestBody
        product: Product,
    ): Mono<Product>

    /**
     * Sample usage:
     * curl -X DELETE $HOST:$PORT/product/1
     *
     * @param productId Id of the product
     */
    @DeleteMapping(
        value = ["/product/{productId}"],
    )
    fun deleteProduct(
        @PathVariable
        productId: Int,
    ): Mono<Void>
}
