package com.thatveryfewthings.api.core.recommendation

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam


interface RecommendationService {

    /**
     * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
     *
     * @param productId Id of the product
     * @return the recommendations of the product
     */
    @GetMapping(
        value = ["/recommendation"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getRecommendations(
        @RequestParam(value = "productId", required = true)
        productId: Int
    ): List<Recommendation>
}