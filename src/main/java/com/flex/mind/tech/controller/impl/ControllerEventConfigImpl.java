package com.flex.mind.tech.controller.impl;

import com.flex.mind.tech.controller.ControllerEventConfig;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.service.ServiceEventConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class ControllerEventConfigImpl implements ControllerEventConfig {

    private final ServiceEventConfig serviceEventConfig;

    @Override
    public ResponseEntity<EventConfigResponseDto> createEventConfig(EventConfigRequestDto eventDto) {
        EventConfigResponseDto response = serviceEventConfig.createEventConfig(eventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<EventConfigResponseDto> updateEventConfig(
            String id,
            EventConfigRequestDto eventDto
    ) {
        EventConfigResponseDto eventConfigRequestDto = serviceEventConfig.updateEventConfig(id, eventDto);
        return ResponseEntity.ok(eventConfigRequestDto);
    }

    @Override
    public ResponseEntity<List<EventConfigResponseDto>> getEventConfigs(
            String eventType,
            String source,
            Boolean enabled) {
        List<EventConfigResponseDto> response = serviceEventConfig.getEventConfigs(eventType, source, enabled);
        return ResponseEntity.ok(response);
    }
}
