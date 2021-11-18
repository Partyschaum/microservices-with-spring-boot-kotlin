package com.thatveryfewthings

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer

abstract class MongoDbTest {

    companion object {
        private val database = MongoDBContainer("mongo:3.6.9").also {
            it.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun setProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.host") { database.host }
            registry.add("spring.data.mongodb.port") { database.firstMappedPort }
        }
    }
}
