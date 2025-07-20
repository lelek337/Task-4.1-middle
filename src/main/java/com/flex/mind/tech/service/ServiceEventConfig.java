package com.flex.mind.tech.service;

import com.flex.mind.tech.exception.EventConfigAlreadyExistsException;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.service.strategy.EventConfigStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceEventConfig {

    private final EventConfigStorageStrategy storageStrategy;

    public EventConfigResponseDto createEventConfig(EventConfigRequestDto requestDto) {
        if (storageStrategy.existsByEventTypeAndSource(requestDto.getEventType(), requestDto.getSource())) {
            throw new EventConfigAlreadyExistsException("EventConfig already exists");
        }

        EventConfigRequestDto requestWithId = EventConfigRequestDto.builder()
                .id(UUID.randomUUID().toString())
                .eventType(requestDto.getEventType())
                .source(requestDto.getSource())
                .enabled(requestDto.getEnabled())
                .build();

        return storageStrategy.createEventConfig(requestWithId);
    }

    public EventConfigResponseDto updateEventConfig(String id, EventConfigRequestDto requestDto) {
        return storageStrategy.updateEventConfig(id, requestDto);
    }

    public List<EventConfigResponseDto> getEventConfigs(String eventType, String source, Boolean enabled) {
        return storageStrategy.getEventConfigs(eventType, source, enabled);
    }
}
