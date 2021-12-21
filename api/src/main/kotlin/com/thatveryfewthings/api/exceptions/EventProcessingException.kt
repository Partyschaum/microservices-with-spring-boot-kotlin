package com.thatveryfewthings.api.exceptions

class EventProcessingException : RuntimeException {
    constructor() {}
    constructor(message: String) : super(message) {}
    constructor(message: String, cause: Throwable) : super(message, cause) {}
    constructor(cause: Throwable) : super(cause) {}
}
