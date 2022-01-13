package com.thatveryfewthings.springcloud.authorizationserver.config

import org.springframework.context.annotation.Bean
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@EnableWebSecurity
class DefaultSecurityConfig {

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeRequests { authorizeRequests ->
                authorizeRequests
                    .antMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin(
                Customizer.withDefaults()
            )

        return http.build()
    }

    @Bean
    fun users(): UserDetailsService {
        val user = User.withDefaultPasswordEncoder()
            .username("username")
            .password("password")
            .roles("USER")
            .build()

        return InMemoryUserDetailsManager(user)
    }
}
