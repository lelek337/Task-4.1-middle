package com.flex.mind.tech.controller;

import com.flex.mind.tech.controller.impl.ControllerEventConfigImpl;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.service.ServiceEventConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ControllerEventConfigImpl Unit Tests")
class ControllerEventConfigImplTest {

    @Mock
    private ServiceEventConfig serviceEventConfig;

    @InjectMocks
    private ControllerEventConfigImpl controller;

    private EventConfigRequestDto requestDto;
    private EventConfigResponseDto responseDto;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        requestDto = EventConfigRequestDto.builder()
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .build();

        responseDto = EventConfigResponseDto.builder()
                .id("507f1f77bcf86cd799439011")
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();
    }

    @Test
    @DisplayName("Should create event config successfully")
    void createEventConfig_Success() {
        // Given
        when(serviceEventConfig.createEventConfig(any(EventConfigRequestDto.class)))
                .thenReturn(responseDto);

        // When
        ResponseEntity<EventConfigResponseDto> result = controller.createEventConfig(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        EventConfigResponseDto body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(body.getEventType()).isEqualTo("USER_REGISTRATION");
        assertThat(body.getSource()).isEqualTo("auth-service");
        assertThat(body.getEnabled()).isTrue();
        assertThat(body.getCreatedAt()).isEqualTo(fixedTime);
        assertThat(body.getUpdatedAt()).isEqualTo(fixedTime);

        verify(serviceEventConfig, times(1)).createEventConfig(requestDto);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should handle validation errors")
    void createEventConfig_InvalidData_ThrowsValidationException() {
        // Given
        EventConfigRequestDto invalidDto = EventConfigRequestDto.builder()
                .eventType("")
                .source(null)
                .build();

        when(serviceEventConfig.createEventConfig(invalidDto))
                .thenThrow(new IllegalArgumentException("Invalid request data"));

        // When & Then
        assertThatThrownBy(() -> controller.createEventConfig(invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid request data");

        verify(serviceEventConfig, times(1)).createEventConfig(invalidDto);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should handle null request in createEventConfig")
    void createEventConfig_WithNullRequest_ThrowsException() {
        // Given
        when(serviceEventConfig.createEventConfig(null))
                .thenThrow(new IllegalArgumentException("Request cannot be null"));

        // When & Then
        assertThatThrownBy(() -> controller.createEventConfig(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request cannot be null");

        verify(serviceEventConfig, times(1)).createEventConfig(null);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should update event config successfully")
    void updateEventConfig_Success() {
        // Given
        String eventId = "507f1f77bcf86cd799439011";
        LocalDateTime updateTime = LocalDateTime.of(2024, 1, 15, 11, 45, 0);

        EventConfigResponseDto updatedResponse = EventConfigResponseDto.builder()
                .id(eventId)
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .createdAt(fixedTime)
                .updatedAt(updateTime)
                .build();

        EventConfigRequestDto updateRequest = EventConfigRequestDto.builder()
                .eventType("USER_LOGIN")
                .source("auth-service")
                .build();

        when(serviceEventConfig.updateEventConfig(eq(eventId), any(EventConfigRequestDto.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<EventConfigResponseDto> result = controller.updateEventConfig(eventId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        EventConfigResponseDto body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(eventId);
        assertThat(body.getEventType()).isEqualTo("USER_LOGIN");
        assertThat(body.getSource()).isEqualTo("auth-service");
        assertThat(body.getEnabled()).isFalse();
        assertThat(body.getCreatedAt()).isEqualTo(fixedTime);
        assertThat(body.getUpdatedAt()).isEqualTo(updateTime);

        verify(serviceEventConfig, times(1)).updateEventConfig(eventId, updateRequest);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should handle empty ID in updateEventConfig")
    void updateEventConfig_WithEmptyId_ThrowsException() {
        // Given
        String emptyId = "";
        when(serviceEventConfig.updateEventConfig(eq(emptyId), any(EventConfigRequestDto.class)))
                .thenThrow(new IllegalArgumentException("ID cannot be empty"));

        // When & Then
        assertThatThrownBy(() -> controller.updateEventConfig(emptyId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID cannot be empty");

        verify(serviceEventConfig, times(1)).updateEventConfig(emptyId, requestDto);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should get event configs with all parameters successfully")
    void getEventConfigs_WithAllParameters_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = "auth-service";
        Boolean enabled = true;

        EventConfigResponseDto config1 = EventConfigResponseDto.builder()
                .id("507f1f77bcf86cd799439011")
                .eventType(eventType)
                .source(source)
                .enabled(enabled)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        EventConfigResponseDto config2 = EventConfigResponseDto.builder()
                .id("507f1f77bcf86cd799439012")
                .eventType(eventType)
                .source(source)
                .enabled(enabled)
                .createdAt(fixedTime.plusMinutes(5))
                .updatedAt(fixedTime.plusMinutes(5))
                .build();

        List<EventConfigResponseDto> expectedConfigs = Arrays.asList(config1, config2);

        when(serviceEventConfig.getEventConfigs(eventType, source, enabled))
                .thenReturn(expectedConfigs);

        // When
        ResponseEntity<List<EventConfigResponseDto>> result =
                controller.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<EventConfigResponseDto> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(2);

        // Проверяем первый элемент
        EventConfigResponseDto firstConfig = body.get(0);
        assertThat(firstConfig.getId()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(firstConfig.getEventType()).isEqualTo(eventType);
        assertThat(firstConfig.getSource()).isEqualTo(source);
        assertThat(firstConfig.getEnabled()).isEqualTo(enabled);
        assertThat(firstConfig.getCreatedAt()).isEqualTo(fixedTime);
        assertThat(firstConfig.getUpdatedAt()).isEqualTo(fixedTime);

        // Проверяем второй элемент
        EventConfigResponseDto secondConfig = body.get(1);
        assertThat(secondConfig.getId()).isEqualTo("507f1f77bcf86cd799439012");
        assertThat(secondConfig.getEventType()).isEqualTo(eventType);
        assertThat(secondConfig.getSource()).isEqualTo(source);
        assertThat(secondConfig.getEnabled()).isEqualTo(enabled);

        verify(serviceEventConfig, times(1)).getEventConfigs(eventType, source, enabled);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should get all event configs when parameters are null")
    void getEventConfigs_WithNullParameters_Success() {
        // Given
        List<EventConfigResponseDto> allConfigs = Arrays.asList(
                EventConfigResponseDto.builder()
                        .id("507f1f77bcf86cd799439011")
                        .eventType("USER_REGISTRATION")
                        .source("auth-service")
                        .enabled(true)
                        .createdAt(fixedTime)
                        .updatedAt(fixedTime)
                        .build(),
                EventConfigResponseDto.builder()
                        .id("507f1f77bcf86cd799439012")
                        .eventType("USER_LOGIN")
                        .source("payment-service")
                        .enabled(false)
                        .createdAt(fixedTime.plusHours(1))
                        .updatedAt(fixedTime.plusHours(2))
                        .build()
        );

        when(serviceEventConfig.getEventConfigs(null, null, null))
                .thenReturn(allConfigs);

        // When
        ResponseEntity<List<EventConfigResponseDto>> result =
                controller.getEventConfigs(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<EventConfigResponseDto> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(2);
        assertThat(body.get(0).getEventType()).isEqualTo("USER_REGISTRATION");
        assertThat(body.get(1).getEventType()).isEqualTo("USER_LOGIN");

        verify(serviceEventConfig, times(1)).getEventConfigs(null, null, null);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should get filtered event configs with partial parameters")
    void getEventConfigs_WithPartialParameters_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        List<EventConfigResponseDto> filteredConfigs = Arrays.asList(responseDto);

        when(serviceEventConfig.getEventConfigs(eventType, null, null))
                .thenReturn(filteredConfigs);

        // When
        ResponseEntity<List<EventConfigResponseDto>> result =
                controller.getEventConfigs(eventType, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<EventConfigResponseDto> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).getEventType()).isEqualTo(eventType);
        assertThat(body.get(0).getId()).isEqualTo("507f1f77bcf86cd799439011");

        verify(serviceEventConfig, times(1)).getEventConfigs(eventType, null, null);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should return empty list when no configs found")
    void getEventConfigs_EmptyResult_Success() {
        // Given
        when(serviceEventConfig.getEventConfigs(anyString(), anyString(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<EventConfigResponseDto>> result =
                controller.getEventConfigs("UNKNOWN_TYPE", "unknown-service", true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<EventConfigResponseDto> body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body).isEmpty();

        verify(serviceEventConfig, times(1)).getEventConfigs("UNKNOWN_TYPE", "unknown-service", true);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should handle service exception in createEventConfig")
    void createEventConfig_ServiceException_ThrowsException() {
        // Given
        when(serviceEventConfig.createEventConfig(any(EventConfigRequestDto.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> controller.createEventConfig(requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(serviceEventConfig, times(1)).createEventConfig(requestDto);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should handle service exception in updateEventConfig")
    void updateEventConfig_ServiceException_ThrowsException() {
        // Given
        String eventId = "507f1f77bcf86cd799439011";
        when(serviceEventConfig.updateEventConfig(eq(eventId), any(EventConfigRequestDto.class)))
                .thenThrow(new RuntimeException("Event config not found"));

        // When & Then
        assertThatThrownBy(() -> controller.updateEventConfig(eventId, requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Event config not found");

        verify(serviceEventConfig, times(1)).updateEventConfig(eventId, requestDto);
        verifyNoMoreInteractions(serviceEventConfig);
    }

    @Test
    @DisplayName("Should verify response DTO structure")
    void verifyResponseDtoStructure() {
        // Given
        EventConfigResponseDto dto = EventConfigResponseDto.builder()
                .id("test-id")
                .eventType("TEST_EVENT")
                .source("test-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime.plusMinutes(10))
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo("test-id");
        assertThat(dto.getEventType()).isEqualTo("TEST_EVENT");
        assertThat(dto.getSource()).isEqualTo("test-service");
        assertThat(dto.getEnabled()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(fixedTime);
        assertThat(dto.getUpdatedAt()).isEqualTo(fixedTime.plusMinutes(10));
    }
}