package com.thatveryfewthings.microservices.core.review.persistence

import com.thatveryfewthings.util.mapstruct.Default
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

    @Default
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
