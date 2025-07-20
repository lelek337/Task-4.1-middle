package com.flex.mind.tech.service.strategy;

import com.flex.mind.tech.exception.EventConfigNotFoundException;
import com.flex.mind.tech.model.entity.EventConfigMongo;
import com.flex.mind.tech.model.mapper.EventConfigMapper;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.repository.EventConfigMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MongoEventConfigService Unit Tests")
class MongoEventConfigServiceTest {

    @Mock
    private EventConfigMongoRepository repository;

    @Mock
    private EventConfigMapper mapper;

    @InjectMocks
    private MongoEventConfigService service;

    private EventConfigRequestDto requestDto;
    private EventConfigMongo mongoEntity;
    private EventConfigResponseDto responseDto;
    private LocalDateTime fixedTime;
    private final String TEST_ID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        requestDto = EventConfigRequestDto.builder()
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .build();

        mongoEntity = EventConfigMongo.builder()
                .id(TEST_ID)
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        responseDto = EventConfigResponseDto.builder()
                .id(TEST_ID)
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
        EventConfigMongo entityToSave = EventConfigMongo.builder()
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .build();

        when(mapper.toMongoEntity(requestDto)).thenReturn(entityToSave);

        // ✅ Настраиваем repository.save() для возврата entity с установленными полями
        when(repository.save(any(EventConfigMongo.class))).thenAnswer(invocation -> {
            EventConfigMongo entity = invocation.getArgument(0);
            return EventConfigMongo.builder()
                    .id(entity.getId())
                    .eventType(entity.getEventType())
                    .source(entity.getSource())
                    .enabled(entity.getEnabled())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        });

        when(mapper.toResponseDto(any(EventConfigMongo.class))).thenReturn(responseDto);

        // When
        EventConfigResponseDto result = service.createEventConfig(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ID);
        assertThat(result.getEventType()).isEqualTo("USER_REGISTRATION");
        assertThat(result.getSource()).isEqualTo("auth-service");
        assertThat(result.getEnabled()).isTrue();

        verify(mapper, times(1)).toMongoEntity(requestDto);
        verify(repository, times(1)).save(any(EventConfigMongo.class));
        verify(mapper, times(1)).toResponseDto(any(EventConfigMongo.class));

        ArgumentCaptor<EventConfigMongo> entityCaptor = ArgumentCaptor.forClass(EventConfigMongo.class);
        verify(repository).save(entityCaptor.capture());
        EventConfigMongo savedEntity = entityCaptor.getValue();

        assertThat(savedEntity.getId()).isNotNull();
        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getUpdatedAt()).isNotNull();
        assertThat(savedEntity.getEventType()).isEqualTo("USER_REGISTRATION");
        assertThat(savedEntity.getSource()).isEqualTo("auth-service");
    }

    @Test
    @DisplayName("Should update event config successfully")
    void updateEventConfig_Success() {
        // Given
        String eventId = TEST_ID;
        EventConfigRequestDto updateRequest = EventConfigRequestDto.builder()
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .build();

        EventConfigMongo existingEntity = EventConfigMongo.builder()
                .id(eventId)
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        EventConfigMongo updatedEntity = EventConfigMongo.builder()
                .id(eventId)
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .createdAt(fixedTime)
                .updatedAt(fixedTime.plusMinutes(30))
                .build();

        EventConfigResponseDto updatedResponse = EventConfigResponseDto.builder()
                .id(eventId)
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .createdAt(fixedTime)
                .updatedAt(fixedTime.plusMinutes(30))
                .build();

        when(repository.findById(eventId)).thenReturn(Optional.of(existingEntity));
        when(repository.save(existingEntity)).thenReturn(updatedEntity);
        when(mapper.toResponseDto(updatedEntity)).thenReturn(updatedResponse);

        // When
        EventConfigResponseDto result = service.updateEventConfig(eventId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(eventId);
        assertThat(result.getEventType()).isEqualTo("USER_LOGIN");
        assertThat(result.getSource()).isEqualTo("auth-service");
        assertThat(result.getEnabled()).isFalse();

        verify(repository, times(1)).findById(eventId);
        verify(repository, times(1)).save(existingEntity);
        verify(mapper, times(1)).toResponseDto(updatedEntity);

        assertThat(existingEntity.getEventType()).isEqualTo("USER_LOGIN");
        assertThat(existingEntity.getSource()).isEqualTo("auth-service");
        assertThat(existingEntity.getEnabled()).isFalse();
        assertThat(existingEntity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent event config")
    void updateEventConfig_NotFound_ThrowsException() {
        // Given
        String nonExistentId = "non-existent-id";
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.updateEventConfig(nonExistentId, requestDto))
                .isInstanceOf(EventConfigNotFoundException.class)
                .hasMessage("Event config not found with id: " + nonExistentId);

        verify(repository, times(1)).findById(nonExistentId);
        verify(repository, never()).save(any(EventConfigMongo.class));
        verify(mapper, never()).toResponseDto(any(EventConfigMongo.class));
    }

    @Test
    @DisplayName("Should get all event configs when no filters provided")
    void getEventConfigs_NoFilters_Success() {
        // Given
        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findAll()).thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDto);

        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).toResponseDto(mongoEntity);
    }

    @Test
    @DisplayName("Should get event configs with all filters")
    void getEventConfigs_AllFilters_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = "auth-service";
        Boolean enabled = true;

        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findByEventTypeAndSourceAndEnabled(eventType, source, enabled))
                .thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo(eventType);
        assertThat(result.get(0).getSource()).isEqualTo(source);
        assertThat(result.get(0).getEnabled()).isEqualTo(enabled);

        verify(repository, times(1)).findByEventTypeAndSourceAndEnabled(eventType, source, enabled);
        verify(mapper, times(1)).toResponseDto(mongoEntity);
    }

    @Test
    @DisplayName("Should get event configs with eventType and source filters")
    void getEventConfigs_EventTypeAndSource_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = "auth-service";
        Boolean enabled = null;

        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findByEventTypeAndSource(eventType, source)).thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).hasSize(1);
        verify(repository, times(1)).findByEventTypeAndSource(eventType, source);
        verify(repository, never()).findByEventTypeAndSourceAndEnabled(any(), any(), any());
    }

    @Test
    @DisplayName("Should get event configs with eventType and enabled filters")
    void getEventConfigs_EventTypeAndEnabled_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = null;
        Boolean enabled = true;

        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findByEventTypeAndEnabled(eventType, enabled)).thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).hasSize(1);
        verify(repository, times(1)).findByEventTypeAndEnabled(eventType, enabled);
    }

    @Test
    @DisplayName("Should get event configs with source and enabled filters")
    void getEventConfigs_SourceAndEnabled_Success() {
        // Given
        String eventType = null;
        String source = "auth-service";
        Boolean enabled = true;

        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findBySourceAndEnabled(source, enabled)).thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).hasSize(1);
        verify(repository, times(1)).findBySourceAndEnabled(source, enabled);
    }

    @Test
    @DisplayName("Should get event configs with eventType filter only")
    void getEventConfigs_EventTypeOnly_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findByEventType(eventType)).thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, null, null);

        // Then
        assertThat(result).hasSize(1);
        verify(repository, times(1)).findByEventType(eventType);
    }

    @Test
    @DisplayName("Should get event configs with source filter only")
    void getEventConfigs_SourceOnly_Success() {
        // Given
        String source = "auth-service";
        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findBySource(source)).thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(null, source, null);

        // Then
        assertThat(result).hasSize(1);
        verify(repository, times(1)).findBySource(source);
    }

    @Test
    @DisplayName("Should get event configs with enabled filter only")
    void getEventConfigs_EnabledOnly_Success() {
        // Given
        Boolean enabled = true;
        List<EventConfigMongo> entities = Arrays.asList(mongoEntity);
        when(repository.findByEnabled(enabled)).thenReturn(entities);
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(null, null, enabled);

        // Then
        assertThat(result).hasSize(1);
        verify(repository, times(1)).findByEnabled(enabled);
    }

    @Test
    @DisplayName("Should return empty list when no configs found")
    void getEventConfigs_EmptyResult_Success() {
        // Given
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(repository, times(1)).findAll();
        verify(mapper, never()).toResponseDto(any(EventConfigMongo.class));
    }

    @Test
    @DisplayName("Should check if event config exists by event type and source")
    void existsByEventTypeAndSource_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = "auth-service";
        when(repository.existsByEventTypeAndSource(eventType, source)).thenReturn(true);

        // When
        boolean result = service.existsByEventTypeAndSource(eventType, source);

        // Then
        assertThat(result).isTrue();
        verify(repository, times(1)).existsByEventTypeAndSource(eventType, source);
    }

    @Test
    @DisplayName("Should return false when event config does not exist by event type and source")
    void existsByEventTypeAndSource_NotFound_Success() {
        // Given
        String eventType = "UNKNOWN_EVENT";
        String source = "unknown-service";
        when(repository.existsByEventTypeAndSource(eventType, source)).thenReturn(false);

        // When
        boolean result = service.existsByEventTypeAndSource(eventType, source);

        // Then
        assertThat(result).isFalse();
        verify(repository, times(1)).existsByEventTypeAndSource(eventType, source);
    }

    @Test
    @DisplayName("Should get event configs with multiple entities")
    void getEventConfigs_MultipleEntities_Success() {
        // Given
        EventConfigMongo entity1 = EventConfigMongo.builder()
                .id("id1")
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        EventConfigMongo entity2 = EventConfigMongo.builder()
                .id("id2")
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .createdAt(fixedTime.plusMinutes(10))
                .updatedAt(fixedTime.plusMinutes(10))
                .build();

        EventConfigResponseDto response1 = EventConfigResponseDto.builder()
                .id("id1")
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .build();

        EventConfigResponseDto response2 = EventConfigResponseDto.builder()
                .id("id2")
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .build();

        List<EventConfigMongo> entities = Arrays.asList(entity1, entity2);
        when(repository.findBySource("auth-service")).thenReturn(entities);
        when(mapper.toResponseDto(entity1)).thenReturn(response1);
        when(mapper.toResponseDto(entity2)).thenReturn(response2);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(null, "auth-service", null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(response1, response2);

        verify(repository, times(1)).findBySource("auth-service");
        verify(mapper, times(1)).toResponseDto(entity1);
        verify(mapper, times(1)).toResponseDto(entity2);
    }

    @Test
    @DisplayName("Should handle repository exception during create")
    void createEventConfig_RepositoryException_ThrowsException() {
        // Given
        EventConfigMongo entityToSave = EventConfigMongo.builder()
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .build();

        when(mapper.toMongoEntity(requestDto)).thenReturn(entityToSave);
        when(repository.save(any(EventConfigMongo.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(mapper, times(1)).toMongoEntity(requestDto);
        verify(repository, times(1)).save(any(EventConfigMongo.class));
        verify(mapper, never()).toResponseDto(any(EventConfigMongo.class));
    }

    @Test
    @DisplayName("Should handle mapper exception during create")
    void createEventConfig_MapperException_ThrowsException() {
        // Given
        when(mapper.toMongoEntity(requestDto))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mapping failed");

        verify(mapper, times(1)).toMongoEntity(requestDto);
        verify(repository, never()).save(any(EventConfigMongo.class));
    }

    @Test
    @DisplayName("Should handle repository exception during update")
    void updateEventConfig_RepositoryException_ThrowsException() {
        // Given
        String eventId = TEST_ID;
        when(repository.findById(eventId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> service.updateEventConfig(eventId, requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(repository, times(1)).findById(eventId);
        verify(repository, never()).save(any(EventConfigMongo.class));
    }

    @Test
    @DisplayName("Should verify all filter combinations are covered")
    void getEventConfigs_AllFilterCombinations_Success() {
        // Test case 1: eventType + source + enabled
        when(repository.findByEventTypeAndSourceAndEnabled("EVENT1", "source1", true))
                .thenReturn(Arrays.asList(mongoEntity));
        when(mapper.toResponseDto(mongoEntity)).thenReturn(responseDto);

        List<EventConfigResponseDto> result1 = service.getEventConfigs("EVENT1", "source1", true);
        assertThat(result1).hasSize(1);
        verify(repository).findByEventTypeAndSourceAndEnabled("EVENT1", "source1", true);

        // Test case 2: eventType + source (enabled = null)
        when(repository.findByEventTypeAndSource("EVENT2", "source2"))
                .thenReturn(Arrays.asList(mongoEntity));

        List<EventConfigResponseDto> result2 = service.getEventConfigs("EVENT2", "source2", null);
        assertThat(result2).hasSize(1);
        verify(repository).findByEventTypeAndSource("EVENT2", "source2");

        // Test case 3: eventType + enabled (source = null)
        when(repository.findByEventTypeAndEnabled("EVENT3", false))
                .thenReturn(Arrays.asList(mongoEntity));

        List<EventConfigResponseDto> result3 = service.getEventConfigs("EVENT3", null, false);
        assertThat(result3).hasSize(1);
        verify(repository).findByEventTypeAndEnabled("EVENT3", false);

        // Test case 4: source + enabled (eventType = null)
        when(repository.findBySourceAndEnabled("source4", true))
                .thenReturn(Arrays.asList(mongoEntity));

        List<EventConfigResponseDto> result4 = service.getEventConfigs(null, "source4", true);
        assertThat(result4).hasSize(1);
        verify(repository).findBySourceAndEnabled("source4", true);

        // Test case 5: eventType only
        when(repository.findByEventType("EVENT5"))
                .thenReturn(Arrays.asList(mongoEntity));

        List<EventConfigResponseDto> result5 = service.getEventConfigs("EVENT5", null, null);
        assertThat(result5).hasSize(1);
        verify(repository).findByEventType("EVENT5");

        // Test case 6: source only
        when(repository.findBySource("source6"))
                .thenReturn(Arrays.asList(mongoEntity));

        List<EventConfigResponseDto> result6 = service.getEventConfigs(null, "source6", null);
        assertThat(result6).hasSize(1);
        verify(repository).findBySource("source6");

        // Test case 7: enabled only
        when(repository.findByEnabled(false))
                .thenReturn(Arrays.asList(mongoEntity));

        List<EventConfigResponseDto> result7 = service.getEventConfigs(null, null, false);
        assertThat(result7).hasSize(1);
        verify(repository).findByEnabled(false);

        // Test case 8: no filters
        when(repository.findAll()).thenReturn(Arrays.asList(mongoEntity));

        List<EventConfigResponseDto> result8 = service.getEventConfigs(null, null, null);
        assertThat(result8).hasSize(1);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should handle null request DTO")
    void createEventConfig_NullRequest_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(null))
                .isInstanceOf(RuntimeException.class); // или NullPointerException в зависимости от mapper

        verify(mapper, times(1)).toMongoEntity(null);
        verify(repository, never()).save(any(EventConfigMongo.class));
    }

    @Test
    @DisplayName("Should handle empty string ID in update")
    void updateEventConfig_EmptyId_ThrowsException() {
        // Given
        String emptyId = "";
        when(repository.findById(emptyId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.updateEventConfig(emptyId, requestDto))
                .isInstanceOf(EventConfigNotFoundException.class)
                .hasMessage("Event config not found with id: " + emptyId);

        verify(repository, times(1)).findById(emptyId);
    }
}