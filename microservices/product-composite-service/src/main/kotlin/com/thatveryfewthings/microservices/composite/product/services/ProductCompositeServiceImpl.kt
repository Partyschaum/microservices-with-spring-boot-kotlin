package com.thatveryfewthings.microservices.composite.product.services

import com.thatveryfewthings.api.composite.product.*
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.api.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.logging.Level

@RestController
class ProductCompositeServiceImpl(
    private val integration: ProductCompositeIntegration,
    private val serviceUtil: ServiceUtil,
) : ProductCompositeService {

    private val log = LoggerFactory.getLogger(javaClass)

    private val nullSecurityContext = SecurityContextImpl()

    override fun getCompositeProduct(productId: Int): Mono<ProductAggregate> {
        log.debug("getCompositeProduct: lookup a product aggregate for productId $productId")

        return try {
            Mono
                .zip(
                    { values ->
                        @Suppress("UNCHECKED_CAST")
                        assembleProductAggregate(
                            values[0] as SecurityContext,
                            values[1] as Product,
                            values[2] as List<Recommendation>,
                            values[3] as List<Review>,
                            serviceUtil.serviceAddress,
                        )
                    },
                    securityContext(),
                    integration.getProduct(productId),
                    integration.getRecommendations(productId).collectList(),
                    integration.getReviews(productId).collectList(),
                )
                .doOnError { log.warn("getCompositeProduct failed: $it") }
                .log(log.name, Level.FINE)
        } catch (ex: RuntimeException) {
            log.warn("getCompositeProduct failed: $ex")
            throw ex
        }
    }

    override fun createCompositeProduct(productAggregate: ProductAggregate): Mono<Void> {
        log.debug("createCompositeProduct: creates a new composite entity for productId: ${productAggregate.productId}")

        return try {
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

            Mono.`when`(
                securityContextLogged(),
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
            ).doOnError { log.warn("createCompositeProduct failed: $it") }
                .log(log.name, Level.FINE)
        } catch (ex: RuntimeException) {
            log.warn("createCompositeProduct failed: $ex")
            throw ex
        }
    }

    override fun deleteCompositeProduct(productId: Int): Mono<Void> {
        log.debug("deleteCompositeProduct: deletes a product aggregate for productId: $productId")

        return try {
            Mono.`when`(
                securityContextLogged(),
                integration.deleteProduct(productId),
                integration.deleteRecommendations(productId),
                integration.deleteReviews(productId),
            ).doOnError { log.warn("deleteCompositeProduct failed $it") }
                .log(log.name, Level.FINE)
        } catch (ex: RuntimeException) {
            log.warn("deleteCompositeProduct failed: $ex")
            throw ex
        }
    }

    private fun assembleProductAggregate(
        securityContext: SecurityContext,
        product: Product,
        recommendations: List<Recommendation>,
        reviews: List<Review>,
        serviceAddress: String
    ): ProductAggregate {

        logAuthorizationInfo(securityContext)

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

    private fun securityContext(): Mono<SecurityContext> {
        return ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSecurityContext)
    }

    private fun securityContextLogged(): Mono<SecurityContext> {
        return securityContext().doOnNext(::logAuthorizationInfo)
    }

    private fun logAuthorizationInfo(securityContext: SecurityContext) {
        val jwtAuthenticationToken = securityContext.authentication
        if (jwtAuthenticationToken != null && jwtAuthenticationToken is JwtAuthenticationToken) {
            logAuthorizationInfo(jwtAuthenticationToken.token)
        } else {
            log.warn("No JWT based authentication supplied, running tests are we?")
        }
    }

    private fun logAuthorizationInfo(jwt: Jwt) {
        if (log.isDebugEnabled) {
            val issuer = jwt.issuer
            val audience = jwt.audience
            val subject = jwt.claims["sub"]
            val scopes = jwt.claims["scope"]
            val expires = jwt.claims["exp"]

            log.debug(
                "Authorization info: Subject: {}, scopes: {}, expires: {}, issuer: {}, audience: {}",
                subject,
                scopes,
                expires,
                issuer,
                audience
            )
        }
    }
}
