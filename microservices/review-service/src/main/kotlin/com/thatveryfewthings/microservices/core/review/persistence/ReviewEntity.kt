package com.thatveryfewthings.microservices.core.review.persistence

import javax.persistence.*

@Entity
@Table(
    name = "reviews",
    indexes = [Index(name = "reviews_unique_idx", unique = true, columnList = "productId, reviewId")]
)
class ReviewEntity(
    @Id
    @GeneratedValue
    var id: Int?,

    @Version
    var version: Int?,

    val productId: Int,
    val reviewId: Int,
    val author: String,
    var subject: String,
    var content: String,
) {
    constructor(
        productId: Int,
        reviewId: Int,
        author: String,
        subject: String,
        content: String,
    ) : this(
        null,
        null,
        productId,
        reviewId,
        author,
        subject,
        content,
    )
}
