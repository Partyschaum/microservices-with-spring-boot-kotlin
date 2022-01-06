package com.thatveryfewthings.microservices.composite.product

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.api.event.Event
import com.thatveryfewthings.microservices.composite.product.EventAssert.Companion.assertThat
import org.junit.jupiter.api.Test

class EventAssertTest {

    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    @Test
    fun testEventAssert() {
        val event1: Event<Int, Product> = Event(Event.Type.CREATE, 1, Product(1, "name", 1, null))
        val event2: Event<Int, Product> = Event(Event.Type.CREATE, 1, Product(1, "name", 1, null))
        val event3: Event<Int, Product> = Event(Event.Type.DELETE, 1, null)
        val event4: Event<Int, Product> = Event(Event.Type.CREATE, 1, Product(2, "name", 1, null))

        val event1Json: String = objectMapper.writeValueAsString(event1)

        assertThat(event1Json).isSameEvent(event2)
        assertThat(event1Json).isNotSameEvent(event3)
        assertThat(event1Json).isNotSameEvent(event4)
    }
}
