package com.thatveryfewthings.microservices.composite.product.services

import com.thatveryfewthings.api.composite.product.*
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeServiceImpl(
    private val integration: ProductCompositeIntegration,
    private val serviceUtil: ServiceUtil,
) : ProductCompositeService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getCompositeProduct(productId: Int): ProductAggregate {
        log.debug("getCompositeProduct: lookup a product aggregate for productId $productId")

        val product = integration.getProduct(productId)
        val recommendations = integration.getRecommendations(productId)
        val reviews = integration.getReviews(productId)

        log.debug("getCompositeProduct: aggregate entity found for productId: $product")

        return assembleProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
    }

    override fun createCompositeProduct(productAggregate: ProductAggregate) {
        try {

            log.debug("createCompositeProduct: creates a new composite entity for productId: ${productAggregate.productId}")

            integration.createProduct(
                Product(
                    productId = productAggregate.productId,
                    name = productAggregate.name,
                    weight = productAggregate.weight,
                    serviceAddress = null,
                )
            )

            productAggregate.recommendations.map {
                integration.createRecommendation(
                    Recommendation(
                        productId = productAggregate.productId,
                        recommendationId = it.recommendationId,
                        author = it.author,
                        rate = it.rate,
                        content = it.content,
                        serviceAddress = null,
                    )
                )
            }

            productAggregate.reviews.map {
                integration.createReview(
                    Review(
                        productId = productAggregate.productId,
                        reviewId = it.reviewId,
                        author = it.author,
                        subject = it.subject,
                        content = it.content,
                        serviceAddress = null,
                    )
                )
            }

            log.debug("createCompositeProduct: composite entities created for productId: ${productAggregate.productId}")
        } catch (ex: RuntimeException) {
            log.warn("createCompositeProduct failed", ex)
            throw ex
        }
    }

    override fun deleteCompositeProduct(productId: Int) {
        log.debug("deleteCompositeProduct: deletes a product aggregate for productId: $productId")

        integration.deleteProduct(productId)
        integration.deleteRecommendations(productId)
        integration.deleteReviews(productId)

        log.debug("deleteCompositeProduct: aggregate entities deleted for productId: $productId")
    }

    private fun assembleProductAggregate(
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>,
        serviceAddress: String
    ): ProductAggregate {

        val recommendationSummaries = recommendations.map {
            RecommendationSummary(it.recommendationId, it.author, it.rate, it.content)
        }

        val reviewSummaries = reviews.map {
            ReviewSummary(it.reviewId, it.author, it.subject, it.content)
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
