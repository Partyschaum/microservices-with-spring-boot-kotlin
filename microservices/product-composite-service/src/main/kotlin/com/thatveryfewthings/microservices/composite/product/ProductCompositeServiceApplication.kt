package com.thatveryfewthings.microservices.composite.product

import com.thatveryfewthings.microservices.composite.product.properties.ApiProperties
import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@ComponentScan("com.thatveryfewthings")
@EnableConfigurationProperties(ConfigurationProperties::class, ApiProperties::class)
class ProductCompositeServiceApplication {

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
}

fun main(args: Array<String>) {
    runApplication<ProductCompositeServiceApplication>(*args)
}
