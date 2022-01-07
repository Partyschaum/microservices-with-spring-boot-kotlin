package com.thatveryfewthings.microservices.composite.product.config

import com.thatveryfewthings.microservices.composite.product.services.ProductCompositeIntegration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HealthCheckConfig(
    @Autowired
    private val integration: ProductCompositeIntegration,
) {

    @Bean
    fun coreServices(): CompositeReactiveHealthContributor {
        val registry = mapOf(
            "product" to ReactiveHealthIndicator { integration.getProductHealth() },
            "recommendation" to ReactiveHealthIndicator { integration.getReviewHealth() },
            "review" to ReactiveHealthIndicator { integration.getRecommendationHealth() },
        )

        return CompositeReactiveHealthContributor.fromMap(registry)
    }
}
