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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventConfigMongo toMongoEntity(EventConfigRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventConfigElastic toElasticEntity(EventConfigRequestDto requestDto);

    EventConfigResponseDto toResponseDto(EventConfigMongo entity);

    EventConfigResponseDto toResponseDto(EventConfigElastic entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMongoEntity(@MappingTarget EventConfigMongo entity, EventConfigRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateElasticEntity(@MappingTarget EventConfigElastic entity, EventConfigRequestDto requestDto);
}
