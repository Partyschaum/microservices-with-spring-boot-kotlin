package com.thatveryfewthings.microservices.composite.product

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.thatveryfewthings.api.event.Event
import org.assertj.core.api.AbstractAssert
import org.slf4j.LoggerFactory
import java.io.IOException

class EventAssert(actual: String) : AbstractAssert<EventAssert, String>(actual, EventAssert::class.java) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    companion object {
        fun assertThat(actual: String) = EventAssert(actual)
    }

    fun <K, T> isSameEvent(expected: Event<K, T>): EventAssert {
        if (!isEqual(expected)) {
            failWithMessage("Expected event to match $expected but was $actual")
        }

        return this
    }

    fun <K, T> isNotSameEvent(expected: Event<K, T>): EventAssert {
        if (isEqual(expected)) {
            failWithMessage("Expected event to differ from ")
        }

        return this
    }

    private fun <K, T> isEqual(expected: Event<K, T>): Boolean {
        log.trace("Convert the following JSON string to a map: $actual")

        val eventAsMap = convertJsonStringToMap(actual).apply {
            remove("eventCreatedAt")
        }

        val expectedEventAsMap = convertEventToMap(expected)

        log.trace("Got the map: $eventAsMap")
        log.trace("Compare to the expected map: $expectedEventAsMap")

        return eventAsMap == expectedEventAsMap
    }

    private fun convertJsonStringToMap(actual: String): HashMap<String, *> {
        return try {
            val typeReference = object : TypeReference<HashMap<String, *>>() {}
            objectMapper.readValue(actual, typeReference)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }

    private fun <K, T> convertEventToMap(expected: Event<K, T>): MutableMap<*, *> {
        return convertObjectToMap(expected).apply {
            remove("eventCreatedAt")
        }
    }

    private fun convertObjectToMap(any: Any): MutableMap<*, *> {
        val node = objectMapper.convertValue(any, JsonNode::class.java)
        return objectMapper.convertValue(node, MutableMap::class.java)
    }
}
