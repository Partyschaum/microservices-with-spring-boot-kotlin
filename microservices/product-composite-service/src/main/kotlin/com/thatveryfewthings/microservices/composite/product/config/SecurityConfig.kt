package com.thatveryfewthings.microservices.composite.product.config

import com.thatveryfewthings.microservices.composite.product.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .authorizeExchange()
            .pathMatchers("/openapi/**").permitAll()
            .pathMatchers("/webjars/**").permitAll()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers(HttpMethod.POST, "/product-composite/**").hasAuthority("SCOPE_product:write")
            .pathMatchers(HttpMethod.DELETE, "/product-composite/**").hasAuthority("SCOPE_product:write")
            .pathMatchers(HttpMethod.GET, "/product-composite/**").hasAuthority("SCOPE_product:read")
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()

        return http.build()
    }

    @Bean
    fun jwtDecoder(configuration: ConfigurationProperties): ReactiveJwtDecoder {
        return NimbusReactiveJwtDecoder("${configuration.authServer.url}/oauth2/jwks")
    }
}
