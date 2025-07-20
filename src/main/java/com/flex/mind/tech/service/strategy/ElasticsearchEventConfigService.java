package com.flex.mind.tech.service.strategy;

import com.flex.mind.tech.exception.EventConfigNotFoundException;
import com.flex.mind.tech.model.entity.EventConfigElastic;
import com.flex.mind.tech.model.mapper.EventConfigMapper;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.repository.EventConfigElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
@ConditionalOnProperty(name = "app.storage.type", havingValue = "elasticsearch")
public class ElasticsearchEventConfigService implements EventConfigStorageStrategy {

    private final EventConfigElasticsearchRepository repository;
    private final EventConfigMapper mapper;

    @Override
    public EventConfigResponseDto createEventConfig(EventConfigRequestDto requestDto) {
        EventConfigElastic entity = mapper.toElasticEntity(requestDto);
        entity.setId(UUID.randomUUID().toString());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        EventConfigElastic saved = repository.save(entity);
        return mapper.toResponseDto(saved);
    }

    @Override
    public EventConfigResponseDto updateEventConfig(String id, EventConfigRequestDto requestDto) {
        EventConfigElastic existing = repository.findById(id)
                .orElseThrow(() -> new EventConfigNotFoundException("Event config not found with id: " + id));

        existing.setEventType(requestDto.getEventType());
        existing.setSource(requestDto.getSource());
        existing.setEnabled(requestDto.getEnabled());
        existing.setUpdatedAt(LocalDateTime.now());

        EventConfigElastic updated = repository.save(existing);
        return mapper.toResponseDto(updated);
    }

    @Override
    public List<EventConfigResponseDto> getEventConfigs(String eventType, String source, Boolean enabled) {
        List<EventConfigElastic> entities = findWithFilters(eventType, source, enabled);
        return entities.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private List<EventConfigElastic> findWithFilters(String eventType, String source, Boolean enabled) {
        if (eventType != null && source != null && enabled != null) {
            return repository.findByEventTypeAndSourceAndEnabled(eventType, source, enabled);
        }

        if (eventType != null && source != null) {
            return repository.findByEventTypeAndSource(eventType, source);
        }
        if (eventType != null && enabled != null) {
            return repository.findByEventTypeAndEnabled(eventType, enabled);
        }
        if (source != null && enabled != null) {
            return repository.findBySourceAndEnabled(source, enabled);
        }

        if (eventType != null) {
            return repository.findByEventType(eventType);
        }
        if (source != null) {
            return repository.findBySource(source);
        }
        if (enabled != null) {
            return repository.findByEnabled(enabled);
        }

        return repository.findAll();
    }

    @Override
    public boolean existsByEventTypeAndSource(String eventType, String source) {
        return repository.existsByEventTypeAndSource(eventType, source);
    }
}
