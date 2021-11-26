package com.thatveryfewthings.microservices.core.review.services

import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.core.review.ReviewService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.http.ServiceUtil
import com.thatveryfewthings.microservices.core.review.persistence.ReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewServiceImpl(
    private val repository: ReviewRepository,
    private val mapper: ReviewMapper,
    private val serviceUtil: ServiceUtil,
) : ReviewService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getReviews(productId: Int): List<Review> {
        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        val reviewEntities = repository.findByProductId(productId)
        val reviews = mapper.entityListToApiList(reviewEntities).map {
            it.addServiceAddress()
        }

        return reviews.also {
            log.debug("getReviews: response size: ${it.size}")
        }
    }

    override fun createReview(review: Review): Review {
        return try {
            val reviewEntity = mapper.apiToEntity(review)
            val newReview = repository.save(reviewEntity)

            log.debug("createReview: created a review entity: ${review.productId}/${review.reviewId}")

            mapper.entityToApi(newReview).addServiceAddress()
        } catch (ex: DataIntegrityViolationException) {
            throw InvalidInputException("Duplicate key, Product id: ${review.productId}, Review id: ${review.reviewId}")
        }
    }

    override fun deleteReviews(productId: Int) {
        log.debug("deleteReviews: tries to delete reviews for the product with productId: $productId")
        repository.findByProductId(productId).let {
            repository.deleteAll(it)
        }
    }

    private fun Review.addServiceAddress() = copy(
        serviceAddress = serviceUtil.serviceAddress
    )
}
