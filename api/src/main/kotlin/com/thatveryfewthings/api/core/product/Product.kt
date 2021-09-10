package com.thatveryfewthings.api.core.product

data class Product(
    val productId: Int = 0,
    val name: String? = null,
    val weight: Int = 0,
    val serviceAddress: String? = null,
)
