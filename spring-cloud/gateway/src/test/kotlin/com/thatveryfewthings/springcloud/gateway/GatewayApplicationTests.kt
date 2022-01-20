package com.thatveryfewthings.springcloud.gateway

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=some-url",
        "eureka.client.enabled=false",
    ],
)
class GatewayApplicationTests {

    @Test
    fun contextLoads() {
    }
}
