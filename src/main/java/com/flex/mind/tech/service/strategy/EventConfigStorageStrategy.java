package com.flex.mind.tech.service.strategy;

import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;

import java.util.List;

public interface EventConfigStorageStrategy {
    EventConfigResponseDto createEventConfig(EventConfigRequestDto requestDto);

    EventConfigResponseDto updateEventConfig(String id, EventConfigRequestDto requestDto);

    List<EventConfigResponseDto> getEventConfigs(String eventType, String source, Boolean enabled);

    boolean existsByEventTypeAndSource(String eventType, String source);
}
