package com.flex.mind.tech.service;

import com.flex.mind.tech.exception.EventConfigAlreadyExistsException;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.service.strategy.EventConfigStorageStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceEventConfig Unit Tests")
class ServiceEventConfigTest {

    @Mock
    private EventConfigStorageStrategy storageStrategy;

    @InjectMocks
    private ServiceEventConfig service;

    private EventConfigRequestDto requestDto;
    private EventConfigResponseDto responseDto;
    private final String TEST_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private final String EVENT_TYPE = "USER_REGISTRATION";
    private final String SOURCE = "auth-service";
    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    @BeforeEach
    void setUp() {
        requestDto = EventConfigRequestDto.builder()
                .eventType(EVENT_TYPE)
                .source(SOURCE)
                .enabled(true)
                .build();

        responseDto = EventConfigResponseDto.builder()
                .id(TEST_UUID)
                .eventType(EVENT_TYPE)
                .source(SOURCE)
                .enabled(true)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();
    }

    @Test
    @DisplayName("Should create event config successfully when not exists")
    void createEventConfig_Success() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE)).thenReturn(false);
        when(storageStrategy.createEventConfig(any(EventConfigRequestDto.class))).thenReturn(responseDto);

        // When
        EventConfigResponseDto result = service.createEventConfig(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(responseDto);

        // Verify interactions
        verify(storageStrategy, times(1)).existsByEventTypeAndSource(EVENT_TYPE, SOURCE);
        verify(storageStrategy, times(1)).createEventConfig(any(EventConfigRequestDto.class));

        // Verify that the request was enriched with generated ID
        ArgumentCaptor<EventConfigRequestDto> requestCaptor = ArgumentCaptor.forClass(EventConfigRequestDto.class);
        verify(storageStrategy).createEventConfig(requestCaptor.capture());

        EventConfigRequestDto capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getId()).isNotNull();
        assertThat(capturedRequest.getId()).matches(UUID_REGEX);
        assertThat(capturedRequest.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedRequest.getSource()).isEqualTo(SOURCE);
        assertThat(capturedRequest.getEnabled()).isTrue();
    }
    @Test
    @DisplayName("Should throw exception when event config already exists")
    void createEventConfig_AlreadyExists_ThrowsException() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(requestDto))
                .isInstanceOf(EventConfigAlreadyExistsException.class)
                .hasMessage("EventConfig already exists");

        // Verify interactions
        verify(storageStrategy, times(1)).existsByEventTypeAndSource(EVENT_TYPE, SOURCE);
        verify(storageStrategy, never()).createEventConfig(any(EventConfigRequestDto.class));
    }

    @Test
    @DisplayName("Should handle storage strategy exception during existence check")
    void createEventConfig_ExistenceCheckException_ThrowsException() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(storageStrategy, times(1)).existsByEventTypeAndSource(EVENT_TYPE, SOURCE);
        verify(storageStrategy, never()).createEventConfig(any(EventConfigRequestDto.class));
    }

    @Test
    @DisplayName("Should create event config with all request fields preserved")
    void createEventConfig_AllFieldsPreserved_Success() {
        // Given
        EventConfigRequestDto complexRequest = EventConfigRequestDto.builder()
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .build();

        EventConfigResponseDto complexResponse = EventConfigResponseDto.builder()
                .id("any-generated-id")
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .build();

        when(storageStrategy.existsByEventTypeAndSource("USER_LOGIN", "auth-service")).thenReturn(false);
        when(storageStrategy.createEventConfig(any(EventConfigRequestDto.class))).thenReturn(complexResponse);

        // When
        EventConfigResponseDto result = service.createEventConfig(complexRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEventType()).isEqualTo("USER_LOGIN");
        assertThat(result.getEnabled()).isFalse();

        // Verify that all fields were passed through
        ArgumentCaptor<EventConfigRequestDto> requestCaptor = ArgumentCaptor.forClass(EventConfigRequestDto.class);
        verify(storageStrategy).createEventConfig(requestCaptor.capture());

        EventConfigRequestDto capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getId()).isNotNull();
        assertThat(capturedRequest.getEventType()).isEqualTo("USER_LOGIN");
        assertThat(capturedRequest.getSource()).isEqualTo("auth-service");
        assertThat(capturedRequest.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should update event config successfully")
    void updateEventConfig_Success() {
        // Given
        String eventId = "existing-id";
        EventConfigRequestDto updateRequest = EventConfigRequestDto.builder()
                .eventType("USER_LOGOUT")
                .source("auth-service")
                .enabled(true)
                .build();

        EventConfigResponseDto updatedResponse = EventConfigResponseDto.builder()
                .id(eventId)
                .eventType("USER_LOGOUT")
                .source("auth-service")
                .enabled(true)
                .updatedAt(LocalDateTime.now())
                .build();

        when(storageStrategy.updateEventConfig(eventId, updateRequest)).thenReturn(updatedResponse);

        // When
        EventConfigResponseDto result = service.updateEventConfig(eventId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(eventId);
        assertThat(result.getEventType()).isEqualTo("USER_LOGOUT");
        assertThat(result.getEnabled()).isTrue();

        verify(storageStrategy, times(1)).updateEventConfig(eventId, updateRequest);
    }

    @Test
    @DisplayName("Should handle storage strategy exception during update")
    void updateEventConfig_StorageException_ThrowsException() {
        // Given
        String eventId = "existing-id";
        when(storageStrategy.updateEventConfig(eventId, requestDto))
                .thenThrow(new RuntimeException("Update operation failed"));

        // When & Then
        assertThatThrownBy(() -> service.updateEventConfig(eventId, requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Update operation failed");

        verify(storageStrategy, times(1)).updateEventConfig(eventId, requestDto);
    }

    @Test
    @DisplayName("Should get event configs with all filters")
    void getEventConfigs_WithFilters_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = "auth-service";
        Boolean enabled = true;

        List<EventConfigResponseDto> expectedConfigs = Arrays.asList(responseDto);
        when(storageStrategy.getEventConfigs(eventType, source, enabled)).thenReturn(expectedConfigs);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(responseDto);

        verify(storageStrategy, times(1)).getEventConfigs(eventType, source, enabled);
    }

    @Test
    @DisplayName("Should get event configs with partial filters")
    void getEventConfigs_WithPartialFilters_Success() {
        // Given
        String eventType = "USER_LOGIN";
        String source = null;
        Boolean enabled = null;

        EventConfigResponseDto loginResponse = EventConfigResponseDto.builder()
                .id("login-id")
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(true)
                .build();

        List<EventConfigResponseDto> expectedConfigs = Arrays.asList(loginResponse);
        when(storageStrategy.getEventConfigs(eventType, source, enabled)).thenReturn(expectedConfigs);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo("USER_LOGIN");

        verify(storageStrategy, times(1)).getEventConfigs(eventType, source, enabled);
    }

    @Test
    @DisplayName("Should get all event configs when no filters provided")
    void getEventConfigs_NoFilters_Success() {
        // Given
        EventConfigResponseDto config1 = EventConfigResponseDto.builder()
                .id("id1")
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .build();

        EventConfigResponseDto config2 = EventConfigResponseDto.builder()
                .id("id2")
                .eventType("USER_LOGIN")
                .source("payment-service")
                .enabled(false)
                .build();

        List<EventConfigResponseDto> expectedConfigs = Arrays.asList(config1, config2);
        when(storageStrategy.getEventConfigs(null, null, null)).thenReturn(expectedConfigs);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(null, null, null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(config1, config2);

        verify(storageStrategy, times(1)).getEventConfigs(null, null, null);
    }

    @Test
    @DisplayName("Should return empty list when no configs found")
    void getEventConfigs_EmptyResult_Success() {
        // Given
        when(storageStrategy.getEventConfigs(any(), any(), any())).thenReturn(Collections.emptyList());

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs("UNKNOWN", "unknown-service", true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(storageStrategy, times(1)).getEventConfigs("UNKNOWN", "unknown-service", true);
    }

    @Test
    @DisplayName("Should handle storage strategy exception during get")
    void getEventConfigs_StorageException_ThrowsException() {
        // Given
        when(storageStrategy.getEventConfigs(any(), any(), any()))
                .thenThrow(new RuntimeException("Query operation failed"));

        // When & Then
        assertThatThrownBy(() -> service.getEventConfigs("EVENT", "service", true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Query operation failed");

        verify(storageStrategy, times(1)).getEventConfigs("EVENT", "service", true);
    }

    @Test
    @DisplayName("Should handle null request in createEventConfig")
    void createEventConfig_NullRequest_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(null))
                .isInstanceOf(NullPointerException.class);

        // Verify no interactions with storage strategy
        verify(storageStrategy, never()).existsByEventTypeAndSource(any(), any());
        verify(storageStrategy, never()).createEventConfig(any());
    }

    @Test
    @DisplayName("Should handle storage strategy exception during create")
    void createEventConfig_CreateException_ThrowsException() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE)).thenReturn(false);

        // ✅ Мокируем createEventConfig чтобы он бросал исключение
        when(storageStrategy.createEventConfig(any(EventConfigRequestDto.class)))
                .thenThrow(new RuntimeException("Create operation failed"));

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Create operation failed");

        verify(storageStrategy, times(1)).existsByEventTypeAndSource(EVENT_TYPE, SOURCE);
        verify(storageStrategy, times(1)).createEventConfig(any(EventConfigRequestDto.class));
    }

    @Test
    @DisplayName("Should handle request with null eventType and source")
    void createEventConfig_NullEventTypeAndSource_Success() {
        // Given
        EventConfigRequestDto requestWithNulls = EventConfigRequestDto.builder()
                .eventType(null)
                .source(null)
                .enabled(true)
                .build();

        when(storageStrategy.existsByEventTypeAndSource(null, null)).thenReturn(false);
        when(storageStrategy.createEventConfig(any(EventConfigRequestDto.class))).thenReturn(responseDto);

        // When
        EventConfigResponseDto result = service.createEventConfig(requestWithNulls);

        // Then
        assertThat(result).isNotNull();

        verify(storageStrategy, times(1)).existsByEventTypeAndSource(null, null);
        verify(storageStrategy, times(1)).createEventConfig(any(EventConfigRequestDto.class));

        // Verify the enriched request
        ArgumentCaptor<EventConfigRequestDto> requestCaptor = ArgumentCaptor.forClass(EventConfigRequestDto.class);
        verify(storageStrategy).createEventConfig(requestCaptor.capture());

        EventConfigRequestDto capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getId()).isNotNull(); // ID должен быть сгенерирован
        assertThat(capturedRequest.getEventType()).isNull();
        assertThat(capturedRequest.getSource()).isNull();
        assertThat(capturedRequest.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should verify builder pattern creates correct request")
    void createEventConfig_BuilderPattern_Success() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE)).thenReturn(false);
        when(storageStrategy.createEventConfig(any(EventConfigRequestDto.class))).thenReturn(responseDto);

        // When
        service.createEventConfig(requestDto);

        // Then - Verify the builder pattern was used correctly
        ArgumentCaptor<EventConfigRequestDto> requestCaptor = ArgumentCaptor.forClass(EventConfigRequestDto.class);
        verify(storageStrategy).createEventConfig(requestCaptor.capture());

        EventConfigRequestDto builtRequest = requestCaptor.getValue();

        // Verify all original fields are preserved
        assertThat(builtRequest.getEventType()).isEqualTo(requestDto.getEventType());
        assertThat(builtRequest.getSource()).isEqualTo(requestDto.getSource());
        assertThat(builtRequest.getEnabled()).isEqualTo(requestDto.getEnabled());

        // Verify new ID was added
        assertThat(builtRequest.getId()).isNotNull();
        assertThat(builtRequest.getId()).isNotBlank();
        assertThat(builtRequest.getId()).isNotEqualTo(requestDto.getId());
    }

    @Test
    @DisplayName("Should generate valid UUID that can be parsed")
    void createEventConfig_GeneratesValidParsableUUID_Success() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE)).thenReturn(false);
        when(storageStrategy.createEventConfig(any(EventConfigRequestDto.class))).thenReturn(responseDto);

        // When
        service.createEventConfig(requestDto);

        // Then
        ArgumentCaptor<EventConfigRequestDto> requestCaptor = ArgumentCaptor.forClass(EventConfigRequestDto.class);
        verify(storageStrategy).createEventConfig(requestCaptor.capture());

        String generatedId = requestCaptor.getValue().getId();

        assertThatNoException().isThrownBy(() -> {
            UUID parsedUuid = UUID.fromString(generatedId);
            assertThat(parsedUuid.toString()).isEqualTo(generatedId);
        });
    }

    @Test
    @DisplayName("Should generate unique UUIDs for multiple calls")
    void createEventConfig_GeneratesUniqueUUIDs_Success() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE)).thenReturn(false);
        when(storageStrategy.createEventConfig(any(EventConfigRequestDto.class))).thenReturn(responseDto);

        // When
        service.createEventConfig(requestDto);
        service.createEventConfig(requestDto);

        // Then
        ArgumentCaptor<EventConfigRequestDto> requestCaptor = ArgumentCaptor.forClass(EventConfigRequestDto.class);
        verify(storageStrategy, times(2)).createEventConfig(requestCaptor.capture());

        List<EventConfigRequestDto> capturedRequests = requestCaptor.getAllValues();

        String firstId = capturedRequests.get(0).getId();
        String secondId = capturedRequests.get(1).getId();

        assertThat(firstId).isNotEqualTo(secondId);
        assertThat(firstId).isNotNull();
        assertThat(secondId).isNotNull();
    }

    @Test
    @DisplayName("Should handle UUID generation exception")
    void createEventConfig_UuidGenerationException_ThrowsException() {
        // Given
        when(storageStrategy.existsByEventTypeAndSource(EVENT_TYPE, SOURCE)).thenReturn(false);

        try (MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            uuidMock.when(UUID::randomUUID).thenThrow(new RuntimeException("UUID generation failed"));

            // When & Then
            assertThatThrownBy(() -> service.createEventConfig(requestDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("UUID generation failed");

            verify(storageStrategy, times(1)).existsByEventTypeAndSource(EVENT_TYPE, SOURCE);
            verify(storageStrategy, never()).createEventConfig(any(EventConfigRequestDto.class));
        }
    }
}