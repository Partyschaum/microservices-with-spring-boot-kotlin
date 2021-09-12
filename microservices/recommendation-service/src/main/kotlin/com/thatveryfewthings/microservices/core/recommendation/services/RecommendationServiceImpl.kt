package com.thatveryfewthings.microservices.core.recommendation.services

import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.recommendation.RecommendationService
import com.thatveryfewthings.api.exceptions.InvalidInputException
import com.thatveryfewthings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class RecommendationServiceImpl(
    private val serviceUtil: ServiceUtil
) : RecommendationService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getRecommendations(productId: Int): List<Recommendation> {

        if (productId < 1) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        if (productId == 113) {
            log.debug("No recommendations found for productId: $productId")
            return emptyList()
        }

        return listOf(
            Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.serviceAddress),
            Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.serviceAddress),
            Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.serviceAddress),
        ).also {
            log.debug("/recommendation response size: ${it.size}")
        }
    }
}
