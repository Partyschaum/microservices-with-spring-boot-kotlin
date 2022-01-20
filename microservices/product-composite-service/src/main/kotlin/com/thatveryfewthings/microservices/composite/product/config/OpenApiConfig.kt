package com.thatveryfewthings.microservices.composite.product.config

import com.thatveryfewthings.microservices.composite.product.properties.ApiProperties
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SecurityScheme(
    name = "security_auth",
    type = SecuritySchemeType.OAUTH2,
    flows = OAuthFlows(
        authorizationCode = OAuthFlow(
            authorizationUrl = "\${springdoc.o-auth-flow.authorization-url}",
            tokenUrl = "\${springdoc.o-auth-flow.token-url}",
            scopes = [
                OAuthScope(name = "product:read", description = "read scope"),
                OAuthScope(name = "product:write", description = "write scope"),
            ],
        ),
    ),
)
@Configuration
class OpenApiConfig {

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
