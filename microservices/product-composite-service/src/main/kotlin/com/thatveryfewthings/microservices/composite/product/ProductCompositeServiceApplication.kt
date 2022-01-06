package com.thatveryfewthings.microservices.composite.product

import com.thatveryfewthings.microservices.composite.product.properties.ApiProperties
import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.client.RestTemplate
import reactor.core.scheduler.Schedulers

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
@EnableConfigurationProperties(ConfigurationProperties::class, ApiProperties::class)
class ProductCompositeServiceApplication(
    @Value("\${app.publish-event-thread-pool-size}")
    private val threadPoolSize: Int,
    @Value("\${app.publish-event-task-queue-size}")
    private val taskQueueSize: Int,
) {

    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun getOpenApiDocumentation(properties: ApiProperties): OpenAPI {
        return with(properties) {
            OpenAPI()
                .info(
                    Info().title(common.title)
                        .description(common.description)
                        .version(common.version)
                        .contact(
                            Contact()
                                .name(common.contact.name)
                                .url(common.contact.url)
                                .email(common.contact.email)
                        )
                        .termsOfService(common.termsOfService)
                        .license(
                            License()
                                .name(common.license)
                                .url(common.licenseUrl)
                        )
                )
                .externalDocs(
                    ExternalDocumentation()
                        .description(common.externalDocDesc)
                        .url(common.externalDocUrl)
                )
        }
    }

    @Bean
    fun publishEventScheduler() = Schedulers.newBoundedElastic(
        threadPoolSize,
        taskQueueSize,
        "publish-event-pool"
    )
}

fun main(args: Array<String>) {
    runApplication<ProductCompositeServiceApplication>(*args)
}
