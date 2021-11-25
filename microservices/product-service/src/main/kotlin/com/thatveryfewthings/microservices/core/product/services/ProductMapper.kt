package com.thatveryfewthings.microservices.core.product.services

import com.thatveryfewthings.api.core.product.Product
import com.thatveryfewthings.microservices.core.product.persistence.ProductEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface ProductMapper {

    @Mappings(
        Mapping(target = "serviceAddress", ignore = true),
    )
    fun entityToApi(productEntity: ProductEntity): Product

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "version", ignore = true),
    )
    fun apiToEntity(product: Product): ProductEntity
}
