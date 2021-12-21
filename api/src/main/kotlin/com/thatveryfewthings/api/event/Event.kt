package com.thatveryfewthings.api.event

import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

data class Event<K, T>(
    val eventType: Type,
    val key: K,
    val data: T?,
    val eventCreatedAt: ZonedDateTime = now()
) {
    enum class Type {
        CREATE,
        DELETE,
    }
}
