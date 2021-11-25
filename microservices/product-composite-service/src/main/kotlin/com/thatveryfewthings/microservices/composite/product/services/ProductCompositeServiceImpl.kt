package com.thatveryfewthings.microservices.composite.product.services

import com.thatveryfewthings.api.composite.product.*
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.http.ServiceUtil
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeServiceImpl(
    private val integration: ProductCompositeIntegration,
    private val serviceUtil: ServiceUtil,
) : ProductCompositeService {

    override fun getProduct(productId: Int): ProductAggregate {

        val product = integration.getProduct(productId)
        val recommendations = integration.getRecommendations(productId)
        val reviews = integration.getReviews(productId)

        return createProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
    }

    private fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>,
        serviceAddress: String
    ): ProductAggregate {

        val recommendationSummaries = recommendations.map {
            RecommendationSummary(it.recommendationId, it.author, it.rate)
        }

        val reviewSummaries = reviews.map {
            ReviewSummary(it.reviewId, it.author, it.subject)
        }

        val productAddress = product.serviceAddress
        val reviewAddress = if (reviews.isNotEmpty()) reviews.first().serviceAddress else ""
        val recommendationAddress = if (recommendations.isNotEmpty()) recommendations.first().serviceAddress else ""

        return ProductAggregate(
            productId = product.productId,
            name = product.name,
            weight = product.weight,
            recommendations = recommendationSummaries,
            reviews = reviewSummaries,
            serviceAddresses = ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress)
        )
    }
}
