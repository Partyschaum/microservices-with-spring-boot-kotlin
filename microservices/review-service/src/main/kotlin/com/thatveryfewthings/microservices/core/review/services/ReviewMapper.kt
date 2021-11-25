package com.thatveryfewthings.microservices.core.review.services

import com.thatveryfewthings.api.core.review.Review
import com.thatveryfewthings.microservices.core.review.persistence.ReviewEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface ReviewMapper {

    @Mappings(
        Mapping(target = "serviceAddress", ignore = true),
    )
    fun entityToApi(reviewEntity: ReviewEntity): Review

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "version", ignore = true),
    )
    fun apiToEntity(review: Review): ReviewEntity

    fun entityListToApiList(reviewEntities: List<ReviewEntity>): List<Review>
    fun apiListToEntityList(reviews: List<Review>): List<ReviewEntity>
}
