package com.thatveryfewthings.microservices.core.review.services

import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.core.review.ReviewService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewServiceImpl(
    private val serviceUtil: ServiceUtil
) : ReviewService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getReviews(productId: Int): List<Review> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        if (productId == 213) {
            log.debug("No reviews found for productId: $productId")
            return emptyList()
        }

        return listOf(
            Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.serviceAddress),
            Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.serviceAddress),
            Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.serviceAddress),
        ).also {
            log.debug("/reviews response size: ${it.size}")
        }
    }
}
