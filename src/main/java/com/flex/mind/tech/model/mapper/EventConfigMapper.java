package com.flex.mind.tech.model.mapper;

import com.flex.mind.tech.model.entity.EventConfigElastic;
import com.flex.mind.tech.model.entity.EventConfigMongo;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EventConfigMapper {

    // Request DTO to MongoDB Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventConfigMongo toMongoEntity(EventConfigRequestDto requestDto);

    // Request DTO to Elasticsearch Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventConfigElastic toElasticEntity(EventConfigRequestDto requestDto);

    // MongoDB Entity to Response DTO
    EventConfigResponseDto toResponseDto(EventConfigMongo entity);

    // Elasticsearch Entity to Response DTO
    EventConfigResponseDto toResponseDto(EventConfigElastic entity);

    // Update MongoDB Entity from Request DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMongoEntity(@MappingTarget EventConfigMongo entity, EventConfigRequestDto requestDto);

    // Update Elasticsearch Entity from Request DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateElasticEntity(@MappingTarget EventConfigElastic entity, EventConfigRequestDto requestDto);
}
