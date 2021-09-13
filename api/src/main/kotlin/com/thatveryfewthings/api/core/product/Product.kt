package com.thatveryfewthings.api.core.product

data class Product(
    val productId: Int = 0,
    val name: String = "untitled",
    val weight: Int = 0,
    val serviceAddress: String? = null,
)
