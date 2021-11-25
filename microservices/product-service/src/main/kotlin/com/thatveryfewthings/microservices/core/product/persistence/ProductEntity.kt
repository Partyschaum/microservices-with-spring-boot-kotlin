package com.thatveryfewthings.microservices.core.product.persistence

import com.thatveryfewthings.util.mapstruct.Default
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
class ProductEntity(
    @Id
    var id: String?,

    @Version
    var version: Int?,

    @Indexed(unique = true)
    val productId: Int,

    var name: String,
    var weight: Int,
) {

    @Default
    constructor(
        productId: Int,
        name: String,
        weight: Int,
    ) : this(
        null,
        null,
        productId,
        name,
        weight,
    )
}
