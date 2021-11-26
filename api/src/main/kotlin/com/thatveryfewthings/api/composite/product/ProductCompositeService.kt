package com.thatveryfewthings.api.composite.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "ProductComposite", description = "REST API for composite product information")
interface ProductCompositeService {

    /**
     * Sample usage:
     * curl $HOST:$PORT/product-composite/1
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
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getCompositeProduct(
        @PathVariable
        productId: Int,
    ): ProductAggregate

    /**
     * Sample usage:
     * curl -X POST $HOST:$PORT/product-composite \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123, "name": "product 123", "weight":123}'
     *
     * @param productAggregate the JSON structure of the product composite
     * @return the create product composite
     */
    @Operation(
        summary = "\${api.product-composite.create-composite-product.summary}",
        description = "\${api.product-composite.create-composite-product.description}",
    )
    @ApiResponses(
        value = [
            // TODO: Check for 201 response code
            ApiResponse(responseCode = "201", description = "\${api.responseCodes.created}"),
            ApiResponse(responseCode = "400", description = "\${api.responseCodes.badRequest}"),
            ApiResponse(responseCode = "422", description = "\${api.responseCodes.unprocessableEntity}"),
        ]
    )
    @PostMapping(
        value = ["/product-composite"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createCompositeProduct(
        @RequestBody
        productAggregate: ProductAggregate,
    )

    @Operation(
        summary = "\${api.product-composite.delete-composite-product.summary}",
        description = "\${api.product-composite.delete-composite-product.description}",
    )
    @ApiResponses(
        value = [
            // TODO: Check for 200 response code
            ApiResponse(responseCode = "200", description = "\${api.responseCodes.ok}"),
            ApiResponse(responseCode = "400", description = "\${api.responseCodes.badRequest}"),
            ApiResponse(responseCode = "422", description = "\${api.responseCodes.unprocessableEntity}"),
        ]
    )
    @DeleteMapping(
        value = ["/product-composite/{productId}"],
    )
    fun deleteCompositeProduct(
        @PathVariable
        productId: Int,
    )
}
