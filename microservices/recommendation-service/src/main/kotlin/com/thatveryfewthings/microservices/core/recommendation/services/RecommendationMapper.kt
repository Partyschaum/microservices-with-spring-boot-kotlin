package com.thatveryfewthings.microservices.core.recommendation.services

import com.thatveryfewthings.api.core.recommendation.Recommendation
import com.thatveryfewthings.microservices.core.recommendation.persistence.RecommendationEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface RecommendationMapper {

    @Mappings(
        Mapping(target = "rate", source = "recommendationEntity.rating"),
        Mapping(target = "serviceAddress", ignore = true),
    )
    fun entityToApi(recommendationEntity: RecommendationEntity): Recommendation

    @Mappings(
        Mapping(target = "rating", source = "recommendation.rate"),
        Mapping(target = "id", ignore = true),
        Mapping(target = "version", ignore = true),
    )
    fun apiToEntity(recommendation: Recommendation): RecommendationEntity

    fun entityListToApiList(recommendationEntities: List<RecommendationEntity>): List<Recommendation>
    fun apiListToEntityList(recommendations: List<Recommendation>): List<RecommendationEntity>
}
