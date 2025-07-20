package com.flex.mind.tech.service.strategy;

import com.flex.mind.tech.exception.EventConfigNotFoundException;
import com.flex.mind.tech.model.entity.EventConfigMongo;
import com.flex.mind.tech.model.mapper.EventConfigMapper;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.repository.EventConfigMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
@ConditionalOnProperty(name = "app.storage.type", havingValue = "mongodb")
public class MongoEventConfigService implements EventConfigStorageStrategy {

    private final EventConfigMongoRepository repository;
    private final EventConfigMapper mapper;

    @Override
    public EventConfigResponseDto createEventConfig(EventConfigRequestDto requestDto) {
        EventConfigMongo entity = mapper.toMongoEntity(requestDto);
        entity.setId(UUID.randomUUID().toString());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        EventConfigMongo saved = repository.save(entity);
        return mapper.toResponseDto(saved);
    }

    @Override
    public EventConfigResponseDto update(String id, EventConfigRequestDto requestDto) {
        EventConfigMongo existing = repository.findById(id)
                .orElseThrow(() -> new EventConfigNotFoundException("Event config not found with id: " + id));

        existing.setEventType(requestDto.getEventType());
        existing.setSource(requestDto.getSource());
        existing.setEnabled(requestDto.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());

        EventConfigMongo updated = repository.save(existing);
        return mapper.toResponseDto(updated);
    }

    @Override
    public boolean existsByEventTypeAndSource(String eventType, String source) {
        return repository.existsByEventTypeAndSource(eventType, source);
    }
}
