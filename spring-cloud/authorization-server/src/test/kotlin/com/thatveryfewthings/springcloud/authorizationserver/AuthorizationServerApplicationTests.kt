package com.thatveryfewthings.springcloud.authorizationserver

import com.thatveryfewthings.springcloud.authorizationserver.properties.ConfigurationProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@SpringBootTest(
    properties = [
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
    ],
)
@AutoConfigureMockMvc
class AuthorizationServerApplicationTests(
    @Autowired
    private val configuration: ConfigurationProperties,
    @Autowired
    private val mvc: MockMvc,
) {

    @Test
    fun requestTokenUsingClientCredentialsGrantType() {
        mvc.perform(
            MockMvcRequestBuilders
                .post("/oauth2/token")
                .param("grant_type", "client_credentials")
                .header("Authorization", "Basic ${base64encode("writer", configuration.writerSecret)}")
        ).andExpect(MockMvcResultMatchers.status().isOk)

        mvc.perform(
            MockMvcRequestBuilders
                .post("/oauth2/token")
                .param("grant_type", "client_credentials")
                .header("Authorization", "Basic ${base64encode("reader", configuration.readerSecret)}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    private fun base64encode(username: String, password: String) =
        Base64.getEncoder().encodeToString("$username:$password".toByteArray())

    @Test
    fun requestOpenidConfiguration() {
        mvc.perform(MockMvcRequestBuilders.get("/.well-known/openid-configuration"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun requestJwkSet() {
        mvc.perform(MockMvcRequestBuilders.get("/oauth2/jwks"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}

