package com.thatveryfewthings.microservices.core.recommendation.services

import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.recommendation.RecommendationService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.http.ServiceUtil
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.logging.Level

@RestController
class RecommendationServiceImpl(
    private val repository: RecommendationRepository,
    private val mapper: RecommendationMapper,
    private val serviceUtil: ServiceUtil,
) : RecommendationService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getRecommendations(productId: Int): Flux<Recommendation> {
        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        return repository.findByProductId(productId)
            .log(log.name, Level.FINE)
            .map(mapper::entityToApi)
            .map { it.addServiceAddress() }
    }

    override fun createRecommendation(recommendation: Recommendation): Mono<Recommendation> {
        if (recommendation.productId < 1) {
            throw InvalidInputException("Invalid : ${recommendation.productId}")
        }

        val recommendationEntity = mapper.apiToEntity(recommendation)

        return repository.save(recommendationEntity)
            .log(log.name, Level.FINE)
            .onErrorMap(DuplicateKeyException::class.java) {
                InvalidInputException("Duplicate key, Product id: ${recommendation.productId}, Recommendation id: ${recommendation.recommendationId}")
            }
            .map(mapper::entityToApi)
    }

    override fun deleteRecommendations(productId: Int): Mono<Void> {
        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        log.debug("deleteRecommendations: tries to delete recommendations for the product with the productId: $productId")
        return repository
            .deleteAll(
                repository.findByProductId(productId)
                    .log(log.name, Level.FINE)
            )
            .log(log.name, Level.FINE)
    }

    private fun Recommendation.addServiceAddress() = copy(
        serviceAddress = serviceUtil.serviceAddress
    )
}
