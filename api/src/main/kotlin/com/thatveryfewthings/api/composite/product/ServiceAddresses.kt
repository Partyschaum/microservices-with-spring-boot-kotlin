package com.thatveryfewthings.api.composite.product

data class ServiceAddresses(
    val compositeAddress: String? = null,
    val productAddress: String? = null,
    val reviewAddress: String? = null,
    val recommendationAddress: String? = null
)
