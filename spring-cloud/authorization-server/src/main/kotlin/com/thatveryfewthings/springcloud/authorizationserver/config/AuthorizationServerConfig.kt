package com.thatveryfewthings.springcloud.authorizationserver.config

import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import com.thatveryfewthings.springcloud.authorizationserver.jose.JsonWebKeySets
import com.thatveryfewthings.springcloud.authorizationserver.properties.ConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.ClientSettings
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings
import org.springframework.security.oauth2.server.authorization.config.TokenSettings
import java.time.Duration
import java.util.*

@Configuration(proxyBeanMethods = false)
@Import(OAuth2AuthorizationServerConfiguration::class)
class AuthorizationServerConfig(
    private val configuration: ConfigurationProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun registeredClientRepository(): RegisteredClientRepository {
        log.info("Register OAuth client allowing all grant flows...")

        val clientSettings = ClientSettings
            .builder()
            .requireAuthorizationConsent(true)
            .build()

        val tokenSettings = TokenSettings
            .builder()
            .accessTokenTimeToLive(Duration.ofHours(1))
            .build()

        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

        val writerClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("writer")
            .clientSecret(encoder.encode(configuration.writerSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("https://my.redirect.uri")
            .redirectUri("https://localhost:8443/webjars/swagger-ui/oauth2-redirect.html")
            .scope(OidcScopes.OPENID)
            .scope("product:read")
            .scope("product:write")
            .clientSettings(clientSettings)
            .tokenSettings(tokenSettings)
            .build()

        val readerClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("reader")
            .clientSecret(encoder.encode(configuration.readerSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("https://my.redirect.uri")
            .redirectUri("https://localhost:8443/webjars/swagger-ui/oauth2-redirect.html")
            .scope(OidcScopes.OPENID)
            .scope("product:read")
            .clientSettings(clientSettings)
            .tokenSettings(tokenSettings)
            .build()

        return InMemoryRegisteredClientRepository(writerClient, readerClient)
    }

    @Bean
    fun jsonWebKeySource(): JWKSource<SecurityContext> {
        val rsaKey: RSAKey = JsonWebKeySets.generateRsa()
        val jwkSet = JWKSet(rsaKey)

        return JWKSource { jwkSelector: JWKSelector, _: SecurityContext? ->
            jwkSelector.select(jwkSet)
        }
    }

    @Bean
    fun providerSettings(): ProviderSettings = ProviderSettings
        .builder()
        .issuer("http://auth-server:9999")
        .build()
}
