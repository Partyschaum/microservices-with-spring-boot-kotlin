package com.thatveryfewthings.api.core.recommendation

data class Recommendation(
    val productId: Int = 0,
    val recommendationId: Int = 0,
    val author: String? = null,
    val rate: Int = 0,
    val content: String? = null,
    val serviceAddress: String? = null,
)
