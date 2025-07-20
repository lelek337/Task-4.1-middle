package com.flex.mind.tech.controller;

import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.flex.mind.tech.constant.ApiConstant.API_PREFIX;

@Tag(name = "Event Controller", description = "Управление Event")
@RequestMapping(API_PREFIX)
public interface ControllerEventConfig {

    @Operation(summary = "Create EventConfig")
    @PostMapping
    ResponseEntity<EventConfigResponseDto> createEventConfig(@Valid @RequestBody EventConfigRequestDto eventDto);
}
