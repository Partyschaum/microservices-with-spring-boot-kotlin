package com.thatveryfewthings.api.core.review

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReviewService {

    /**
     * Sample usage:
     * curl $HOST:$PORT/review?productId=1
     *
     * @param productId Id of the product
     * @return the reviews of the product
     */
    @GetMapping(
        value = ["/review"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun getReviews(
        @RequestParam(value = "productId", required = true)
        productId: Int,
    ): Flux<Review>

    /**
     * Sample usage:
     * curl -X POST $HOST:$PORT/review \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123, "reviewId":456, "author":"me", "subject":"bla, bla, bla", "content":"yada, yada, yada"}'
     *
     * @param review the JSON structure of the review
     * @return the created review
     */
    @PostMapping(
        value = ["/review"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createReview(
        @RequestBody
        review: Review,
    ): Mono<Review>

    /**
     * Sample usage:
     * curl -X DELETE $HOST:$PORT/review?productId=1
     *
     * @param productId Id of the product
     */
    @DeleteMapping(
        value = ["/review"],
    )
    fun deleteReviews(
        @RequestParam(value = "productId", required = true)
        productId: Int,
    ): Mono<Void>
}
