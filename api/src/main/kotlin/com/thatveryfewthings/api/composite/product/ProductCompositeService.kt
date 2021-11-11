package com.thatveryfewthings.api.composite.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "ProductComposite", description = "REST API for composite product information")
interface ProductCompositeService {

    /**
     * Sample usage: "curl $HOST:$PORT/product-composite/1".
     *
     * @param productId Id of the product
     * @return the composite product info, if found, else null
     */
    @Operation(
        summary = "\${api.product-composite.get-composite-product.summary}",
        description = "\${api.product-composite.get-composite-product.description}",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "\${api.responseCodes.ok}"),
            ApiResponse(responseCode = "400", description = "\${api.responseCodes.badRequest}"),
            ApiResponse(responseCode = "404", description = "\${api.responseCodes.notFound}"),
            ApiResponse(responseCode = "422", description = "\${api.responseCodes.unprocessableEntity}"),
        ]
    )
    @GetMapping(
        value = ["/product-composite/{productId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getProduct(
        @PathVariable
        productId: Int
    ): ProductAggregate
}
