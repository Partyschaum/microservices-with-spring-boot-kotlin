package com.thatveryfewthings.springcloud.gateway.config

import com.thatveryfewthings.springcloud.gateway.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
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
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/headerrouting/**").permitAll()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/eureka/**").permitAll()
            .pathMatchers("/oauth2/**").permitAll()
            .pathMatchers("/login/**").permitAll()
            .pathMatchers("/error/**").permitAll()
            .pathMatchers("/openapi/**").permitAll()
            .pathMatchers("/webjars/**").permitAll()
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
