package com.thatveryfewthings.springcloud.eurekaserver

import com.thatveryfewthings.springcloud.eurekaserver.properties.ConfigurationProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = RANDOM_PORT)
class EurekaServerApplicationTests(
    @Autowired
    private val configuration: ConfigurationProperties,
    @Autowired
    private val client: WebTestClient,
) {

    @Test
    fun contextLoads() {
    }

    @Test
    fun catalogLoads() {
        client.get()
            .uri("/eureka/apps")
            .accept(MediaType.APPLICATION_JSON)
            .headers { it.setBasicAuth(configuration.eurekaUsername, configuration.eurekaPassword) }
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.applications.versions__delta").isEqualTo(1)
            .jsonPath("$.applications.apps__hashcode").isEqualTo("")
            .jsonPath("$.applications.application").isArray
    }

    @Test
    fun healthy() {
        client.get()
            .uri("/actuator/health")
            .accept(MediaType.APPLICATION_JSON)
            .headers { it.setBasicAuth(configuration.eurekaUsername, configuration.eurekaPassword) }
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
    }
}
