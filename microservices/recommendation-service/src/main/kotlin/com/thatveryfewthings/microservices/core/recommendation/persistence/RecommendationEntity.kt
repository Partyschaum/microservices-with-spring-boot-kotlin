package com.thatveryfewthings.microservices.core.recommendation.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId': 1}")
class RecommendationEntity(
    @Id
    var id: String?,

    @Version
    var version: Int?,

    val productId: Int,
    val recommendationId: Int,
    val author: String,
    var rating: Int,
    var content: String,
) {
    constructor(
        productId: Int,
        recommendationId: Int,
        author: String,
        rating: Int,
        content: String,
    ) : this(
        null,
        null,
        productId,
        recommendationId,
        author,
        rating,
        content,
    )
}
