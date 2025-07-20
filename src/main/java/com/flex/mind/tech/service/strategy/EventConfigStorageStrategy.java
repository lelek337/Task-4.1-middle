package com.flex.mind.tech.service.strategy;

import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventConfigStorageStrategy {
    EventConfigResponseDto createEventConfig(EventConfigRequestDto requestDto);
    EventConfigResponseDto update(String id, EventConfigRequestDto requestDto);
    boolean existsByEventTypeAndSource(String eventType, String source);
}
