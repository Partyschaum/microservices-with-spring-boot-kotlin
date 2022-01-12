package com.thatveryfewthings.springcloud.gateway.config

import com.thatveryfewthings.springcloud.gateway.properties.ConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.logging.Level

@Configuration
class HealthCheckConfig(
    webClientBuilder: WebClient.Builder,
    configuration: ConfigurationProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = webClientBuilder.build()

    private val productServiceHost = configuration.productService.url
    private val recommendationServiceHost = configuration.recommendationService.url
    private val reviewServiceHost = configuration.reviewService.url
    private val productCompositeHost = configuration.productCompositeService.url

    @Bean
    fun healthcheckMicroservices(): ReactiveHealthContributor {
        val registry = mapOf(
            "product" to ReactiveHealthIndicator { getHealth(productServiceHost) },
            "recommendation" to ReactiveHealthIndicator { getHealth(recommendationServiceHost) },
            "review" to ReactiveHealthIndicator { getHealth(reviewServiceHost) },
            "product-composite" to ReactiveHealthIndicator { getHealth(productCompositeHost) },
        )

        return CompositeReactiveHealthContributor.fromMap(registry)
    }

    private fun getHealth(baseUrl: String): Mono<Health> {
        val url = "$baseUrl/actuator/health"

        log.debug("Setting up a call to the health API on URL: $url")

        return webClient
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono<String>()
            .map { Health.Builder().up().build() }
            .onErrorResume { Mono.just(Health.Builder().down(it).build()) }
            .log(log.name, Level.FINE)
    }
}
