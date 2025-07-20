package com.flex.mind.tech.controller;

import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.flex.mind.tech.constant.ApiConstant.API_PREFIX;

@Tag(name = "Event Controller", description = "Управление Event")
@RequestMapping(API_PREFIX)
public interface ControllerEventConfig {

    @Operation(
            summary = "Create EventConfig",
            description = "Creates f new event configuration"
    )
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201", description = "Event configuration created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    ResponseEntity<EventConfigResponseDto> createEventConfig(@Valid @RequestBody EventConfigRequestDto eventDto);

    @Operation(
            summary = "Update EventConfig",
            description = "Updates an existing event configuration by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event configuration updated successfully"),
            @ApiResponse(responseCode = "404", description = "Event Configuration not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input date")
    })
    @PutMapping("/{id}")
    ResponseEntity<EventConfigResponseDto> updateEventConfig(
            @PathVariable String id,
            @Valid @RequestBody EventConfigRequestDto eventDto
    );

    @Operation(
            summary = "Get EventConfigs",
            description = "Retrives a list event configurations with optional filtering"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event configurations retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "No configurations found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    ResponseEntity<List<EventConfigResponseDto>> getEventConfigs(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Boolean enabled
    );
}
