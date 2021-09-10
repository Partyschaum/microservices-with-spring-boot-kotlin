package com.thatveryfewthings.api.core.review

data class Review(
    val productId: Int = 0,
    val reviewId: Int = 0,
    val author: String? = null,
    val subject: String? = null,
    val content: String? = null,
    val serviceAddress: String? = null,
)
