package com.thatveryfewthings.microservices.composite.product.services

import com.thatveryfewthings.api.composite.product.*
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.logging.Level

@RestController
class ProductCompositeServiceImpl(
    private val integration: ProductCompositeIntegration,
    private val serviceUtil: ServiceUtil,
) : ProductCompositeService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getCompositeProduct(productId: Int): Mono<ProductAggregate> {
        log.debug("getCompositeProduct: lookup a product aggregate for productId $productId")

        return Mono
            .zip(
                { values ->
                    @Suppress("UNCHECKED_CAST")
                    assembleProductAggregate(
                        values[0] as Product,
                        values[1] as List<Recommendation>,
                        values[2] as List<Review>,
                        serviceUtil.serviceAddress,
                    )
                },
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList(),
            )
            .doOnError { log.warn("getCompositeProduct failed: $it") }
            .log(log.name, Level.FINE)
    }

    override fun createCompositeProduct(productAggregate: ProductAggregate): Mono<Void> {
        log.debug("createCompositeProduct: creates a new composite entity for productId: ${productAggregate.productId}")

        val recommendations = productAggregate.recommendations?.map {
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
        }?.toTypedArray() ?: arrayOf(Mono.empty())

        val reviews = productAggregate.reviews?.map {
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
        }?.toTypedArray() ?: arrayOf(Mono.empty())

        return Mono.`when`(
            integration.createProduct(
                Product(
                    productId = productAggregate.productId,
                    name = productAggregate.name,
                    weight = productAggregate.weight,
                    serviceAddress = null,
                )
            ),
            *recommendations,
            *reviews,
        ).log(log.name, Level.FINE)
    }

    override fun deleteCompositeProduct(productId: Int): Mono<Void> {
        log.debug("deleteCompositeProduct: deletes a product aggregate for productId: $productId")

        return try {
            Mono.zip(
                {},
                integration.deleteProduct(productId),
                integration.deleteRecommendations(productId),
                integration.deleteReviews(productId),
            ).doOnError { log.warn("delete failed $it") }
                .log(log.name, Level.FINE)
                .then()
        } catch (ex: RuntimeException) {
            log.warn("deleteCompositeProduct failed: $ex")
            throw ex
        }
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
            serviceAddresses = ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress),
        )
    }
}
