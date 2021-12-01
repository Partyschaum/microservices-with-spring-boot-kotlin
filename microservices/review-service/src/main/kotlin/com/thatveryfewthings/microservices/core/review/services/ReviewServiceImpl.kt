package com.thatveryfewthings.microservices.core.review.services

import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.core.review.ReviewService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.http.ServiceUtil
import com.thatveryfewthings.microservices.core.review.persistence.ReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import java.util.logging.Level

@RestController
class ReviewServiceImpl(
    private val jdbcScheduler: Scheduler,
    private val repository: ReviewRepository,
    private val mapper: ReviewMapper,
    private val serviceUtil: ServiceUtil,
) : ReviewService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getReviews(productId: Int): Flux<Review> {

        fun byProductId(productId: Int): List<Review> {
            val reviewEntities = repository.findByProductId(productId)
            val reviews = mapper.entityListToApiList(reviewEntities).map {
                it.addServiceAddress()
            }

            return reviews.also {
                log.debug("getReviews: response size: ${it.size}")
            }
        }

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        return Mono.fromCallable { byProductId(productId) }
            .flatMapMany { Flux.fromIterable(it) }
            .subscribeOn(jdbcScheduler)
    }


    override fun createReview(review: Review): Mono<Review> {

        fun create(review: Review): Review {
            val reviewEntity = mapper.apiToEntity(review)
            val newReview = repository.save(reviewEntity)
            log.debug("createReview: created a review entity: ${review.productId}/${review.reviewId}")
            return mapper.entityToApi(newReview).addServiceAddress()
        }

        if (review.productId < 1) {
            throw InvalidInputException("Invalid productId: ${review.productId}")
        }

        return Mono.fromCallable { create(review) }
            .onErrorMap(DataIntegrityViolationException::class.java) {
                InvalidInputException("Duplicate key, Product id: ${review.productId}, Review id: ${review.reviewId}")
            }
            .subscribeOn(jdbcScheduler)
            .log(log.name, Level.FINE)
    }

    override fun deleteReviews(productId: Int): Mono<Void> {

        fun delete(productId: Int) {
            repository.findByProductId(productId).let {
                repository.deleteAll(it)
            }
        }

        log.debug("deleteReviews: tries to delete reviews for the product with productId: $productId")

        return Mono.fromRunnable<Void> { delete(productId) }
            .subscribeOn(jdbcScheduler)
            .then()
    }

    private fun Review.addServiceAddress() = copy(
        serviceAddress = serviceUtil.serviceAddress
    )
}
