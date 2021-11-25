package com.thatveryfewthings.api.core.recommendation

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


interface RecommendationService {

    /**
     * Sample usage:
     * curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId Id of the product
     * @return the recommendations of the product
     */
    @GetMapping(
        value = ["/recommendation"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getRecommendations(
        @RequestParam(value = "productId", required = true)
        productId: Int,
    ): List<Recommendation>

    /**
     * Sample usage:
     * curl -X POST $HOST:$PORT/recommendation \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123, "recommendationId":456, "author":"me", "rate":5, "content":"yada, yada, yada"}'
     *
     * @param recommendation the JSON structure of the recommendation
     * @return the created recommendation
     */
    @PostMapping(
        value = ["/recommendation"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createRecommendation(
        @RequestBody
        recommendation: Recommendation,
    ): Recommendation

    /**
     * Sample usage:
     * curl -X DELETE $HOST:$PORT/recommendation?productId=1
     *
     * @param productId Id of the product
     */
    @DeleteMapping(
        value = ["/recommendation"],
    )
    fun deleteRecommendations(
        @RequestParam(value = "productId", required = true)
        productId: Int
    )
}
