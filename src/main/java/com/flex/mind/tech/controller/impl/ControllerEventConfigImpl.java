package com.flex.mind.tech.controller.impl;

import com.flex.mind.tech.controller.ControllerEventConfig;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.service.strategy.EventConfigStorageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class ControllerEventConfigImpl implements ControllerEventConfig {

    private final EventConfigStorageStrategy serviceEventConfig;

    @Override
    public ResponseEntity<EventConfigResponseDto> createEventConfig(EventConfigRequestDto eventDto) {
        EventConfigResponseDto eventConfigRequestDto = serviceEventConfig.createEventConfig(eventDto);
        return ResponseEntity.ok(eventConfigRequestDto);
    }
}
