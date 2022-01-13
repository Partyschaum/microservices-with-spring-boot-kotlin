package com.thatveryfewthings.springcloud.eurekaserver.config

import com.thatveryfewthings.springcloud.eurekaserver.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories


@Configuration
class SecurityConfig(
    private val configuration: ConfigurationProperties,
) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

        auth.inMemoryAuthentication()
            .withUser(configuration.eurekaUsername)
            .password(encoder.encode(configuration.eurekaPassword))
            .authorities("USER")
    }

    override fun configure(http: HttpSecurity) {
        http
            // Disable CSRF to allow services to register themselves with Eureka
            .csrf().disable()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
    }
}
