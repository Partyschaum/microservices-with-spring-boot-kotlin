package com.thatveryfewthings.microservices.core.recommendation.services

import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.recommendation.RecommendationService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.api.http.ServiceUtil
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController

@RestController
class RecommendationServiceImpl(
    private val repository: RecommendationRepository,
    private val mapper: RecommendationMapper,
    private val serviceUtil: ServiceUtil,
) : RecommendationService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getRecommendations(productId: Int): List<Recommendation> {
        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        val recommendationEntities = repository.findByProductId(productId)
        val recommendations = mapper.entityListToApiList(recommendationEntities).map {
            it.copy(
                serviceAddress = serviceUtil.serviceAddress
            )
        }

        return recommendations.also {
            log.debug("getRecommendation: response size: ${it.size}")
        }
    }

    override fun createRecommendation(recommendation: Recommendation): Recommendation {
        return try {
            val recommendationEntity = mapper.apiToEntity(recommendation)
            val newRecommendation = repository.save(recommendationEntity)

            log.debug("createRecommendation: created a recommendation entity: ${recommendation.productId}/${recommendation.recommendationId}")

            mapper.entityToApi(newRecommendation).copy(
                serviceAddress = serviceUtil.serviceAddress
            )
        } catch (ex: DuplicateKeyException) {
            throw InvalidInputException("Duplicate key, Product id: ${recommendation.productId}, Recommendation id: ${recommendation.recommendationId}")
        }
    }

    override fun deleteRecommendations(productId: Int) {
        log.debug("deleteRecommendations: tries to delete recommendations for the product with the productId: $productId")
        repository.findByProductId(productId).let {
            repository.deleteAll(it)
        }
    }
}
