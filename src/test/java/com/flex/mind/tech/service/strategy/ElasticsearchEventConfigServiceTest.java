package com.flex.mind.tech.service.strategy;

import com.flex.mind.tech.exception.EventConfigNotFoundException;
import com.flex.mind.tech.model.entity.EventConfigElastic;
import com.flex.mind.tech.model.mapper.EventConfigMapper;
import com.flex.mind.tech.model.request.EventConfigRequestDto;
import com.flex.mind.tech.model.response.EventConfigResponseDto;
import com.flex.mind.tech.repository.EventConfigElasticsearchRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElasticsearchEventConfigService Unit Tests")
class ElasticsearchEventConfigServiceTest {

    @Mock
    private EventConfigElasticsearchRepository repository;

    @Mock
    private EventConfigMapper mapper;

    @InjectMocks
    private ElasticsearchEventConfigService service;

    private EventConfigRequestDto requestDto;
    private EventConfigElastic elasticEntity;
    private EventConfigResponseDto responseDto;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        requestDto = EventConfigRequestDto.builder()
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .build();

        elasticEntity = EventConfigElastic.builder()
                .id("test-uuid-123")
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        responseDto = EventConfigResponseDto.builder()
                .id("test-uuid-123")
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
        EventConfigElastic entityToSave = EventConfigElastic.builder()
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .build();

        EventConfigElastic savedEntity = EventConfigElastic.builder()
                .id("generated-uuid")
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        when(mapper.toElasticEntity(requestDto)).thenReturn(entityToSave);
        when(repository.save(any(EventConfigElastic.class))).thenReturn(savedEntity);
        when(mapper.toResponseDto(savedEntity)).thenReturn(responseDto);

        // When
        EventConfigResponseDto result = service.createEventConfig(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(responseDto);

        verify(mapper, times(1)).toElasticEntity(requestDto);
        verify(repository, times(1)).save(any(EventConfigElastic.class));
        verify(mapper, times(1)).toResponseDto(savedEntity);

        ArgumentCaptor<EventConfigElastic> entityCaptor = ArgumentCaptor.forClass(EventConfigElastic.class);
        verify(repository).save(entityCaptor.capture());
        EventConfigElastic capturedEntity = entityCaptor.getValue();

        assertThat(capturedEntity.getId()).isNotNull();
        assertThat(capturedEntity.getCreatedAt()).isNotNull();
        assertThat(capturedEntity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update event config successfully")
    void updateEventConfig_Success() {
        // Given
        String eventId = "test-uuid-123";
        EventConfigRequestDto updateRequest = EventConfigRequestDto.builder()
                .eventType("USER_LOGIN")
                .source("auth-service")
                .enabled(false)
                .build();

        EventConfigElastic existingEntity = EventConfigElastic.builder()
                .id(eventId)
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        EventConfigElastic updatedEntity = EventConfigElastic.builder()
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
        when(repository.save(any(EventConfigElastic.class))).thenReturn(updatedEntity);
        when(mapper.toResponseDto(updatedEntity)).thenReturn(updatedResponse);

        try (MockedStatic<LocalDateTime> timeMock = mockStatic(LocalDateTime.class)) {
            timeMock.when(LocalDateTime::now).thenReturn(fixedTime.plusMinutes(30));

            // When
            EventConfigResponseDto result = service.updateEventConfig(eventId, updateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(eventId);
            assertThat(result.getEventType()).isEqualTo("USER_LOGIN");
            assertThat(result.getSource()).isEqualTo("auth-service");
            assertThat(result.getEnabled()).isFalse();
            assertThat(result.getUpdatedAt()).isEqualTo(fixedTime.plusMinutes(30));

            verify(repository, times(1)).findById(eventId);
            verify(repository, times(1)).save(any(EventConfigElastic.class));
            verify(mapper, times(1)).toResponseDto(updatedEntity);

            assertThat(existingEntity.getEventType()).isEqualTo("USER_LOGIN");
            assertThat(existingEntity.getSource()).isEqualTo("auth-service");
            assertThat(existingEntity.getEnabled()).isFalse();
            assertThat(existingEntity.getUpdatedAt()).isEqualTo(fixedTime.plusMinutes(30));
        }
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
        verify(repository, never()).save(any(EventConfigElastic.class));
        verify(mapper, never()).toResponseDto(any(EventConfigElastic.class));
    }

    @Test
    @DisplayName("Should get event configs with all filters")
    void getEventConfigs_WithAllFilters_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = "auth-service";
        Boolean enabled = true;

        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);

        when(repository.findByEventTypeAndSourceAndEnabled(eventType, source, enabled))
                .thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo(eventType);
        assertThat(result.get(0).getSource()).isEqualTo(source);
        assertThat(result.get(0).getEnabled()).isEqualTo(enabled);

        verify(repository, times(1)).findByEventTypeAndSourceAndEnabled(eventType, source, enabled);
        verify(mapper, times(1)).toResponseDto(elasticEntity);
    }

    @Test
    @DisplayName("Should get event configs with event type and source filters")
    void getEventConfigs_WithEventTypeAndSource_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = "auth-service";
        Boolean enabled = null;

        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);

        when(repository.findByEventTypeAndSource(eventType, source)).thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(repository, times(1)).findByEventTypeAndSource(eventType, source);
        verify(repository, never()).findByEventTypeAndSourceAndEnabled(any(), any(), any());
    }

    @Test
    @DisplayName("Should get event configs with event type and enabled filters")
    void getEventConfigs_WithEventTypeAndEnabled_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = null;
        Boolean enabled = true;

        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);

        when(repository.findByEventTypeAndEnabled(eventType, enabled)).thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(repository, times(1)).findByEventTypeAndEnabled(eventType, enabled);
    }

    @Test
    @DisplayName("Should get event configs with source and enabled filters")
    void getEventConfigs_WithSourceAndEnabled_Success() {
        // Given
        String eventType = null;
        String source = "auth-service";
        Boolean enabled = true;

        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);

        when(repository.findBySourceAndEnabled(source, enabled)).thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(repository, times(1)).findBySourceAndEnabled(source, enabled);
    }

    @Test
    @DisplayName("Should get event configs with single event type filter")
    void getEventConfigs_WithEventTypeOnly_Success() {
        // Given
        String eventType = "USER_REGISTRATION";
        String source = null;
        Boolean enabled = null;

        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);

        when(repository.findByEventType(eventType)).thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(repository, times(1)).findByEventType(eventType);
    }

    @Test
    @DisplayName("Should get event configs with single source filter")
    void getEventConfigs_WithSourceOnly_Success() {
        // Given
        String eventType = null;
        String source = "auth-service";
        Boolean enabled = null;

        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);

        when(repository.findBySource(source)).thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(repository, times(1)).findBySource(source);
    }

    @Test
    @DisplayName("Should get event configs with single enabled filter")
    void getEventConfigs_WithEnabledOnly_Success() {
        // Given
        String eventType = null;
        String source = null;
        Boolean enabled = true;

        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);

        when(repository.findByEnabled(enabled)).thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(eventType, source, enabled);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(repository, times(1)).findByEnabled(enabled);
    }

    // ✅ НОВЫЕ ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ:

    @Test
    @DisplayName("Should get all event configs when no filters provided")
    void getEventConfigs_NoFilters_Success() {
        // Given
        List<EventConfigElastic> entities = Arrays.asList(elasticEntity);
        when(repository.findAll()).thenReturn(entities);
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        // When
        List<EventConfigResponseDto> result = service.getEventConfigs(null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDto);

        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).toResponseDto(elasticEntity);
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
        verify(mapper, never()).toResponseDto(any(EventConfigElastic.class));
    }

    @Test
    @DisplayName("Should check if event config exists by event type and source")
    void existsByEventTypeAndSource_Exists_Success() {
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
    void existsByEventTypeAndSource_NotExists_Success() {
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
    @DisplayName("Should handle multiple entities in getEventConfigs")
    void getEventConfigs_MultipleEntities_Success() {
        // Given
        EventConfigElastic entity1 = EventConfigElastic.builder()
                .id("id1")
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        EventConfigElastic entity2 = EventConfigElastic.builder()
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

        List<EventConfigElastic> entities = Arrays.asList(entity1, entity2);
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
        EventConfigElastic entityToSave = EventConfigElastic.builder()
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .build();

        when(mapper.toElasticEntity(requestDto)).thenReturn(entityToSave);
        when(repository.save(any(EventConfigElastic.class)))
                .thenThrow(new RuntimeException("Elasticsearch connection failed"));

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Elasticsearch connection failed");

        verify(mapper, times(1)).toElasticEntity(requestDto);
        verify(repository, times(1)).save(any(EventConfigElastic.class));
        verify(mapper, never()).toResponseDto(any(EventConfigElastic.class));
    }

    @Test
    @DisplayName("Should handle mapper exception during create")
    void createEventConfig_MapperException_ThrowsException() {
        // Given
        when(mapper.toElasticEntity(requestDto))
                .thenThrow(new RuntimeException("Mapping failed"));

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mapping failed");

        verify(mapper, times(1)).toElasticEntity(requestDto);
        verify(repository, never()).save(any(EventConfigElastic.class));
    }

    @Test
    @DisplayName("Should handle repository exception during update")
    void updateEventConfig_RepositoryException_ThrowsException() {
        // Given
        String eventId = "test-uuid-123";
        when(repository.findById(eventId))
                .thenThrow(new RuntimeException("Elasticsearch connection failed"));

        // When & Then
        assertThatThrownBy(() -> service.updateEventConfig(eventId, requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Elasticsearch connection failed");

        verify(repository, times(1)).findById(eventId);
        verify(repository, never()).save(any(EventConfigElastic.class));
    }

    @Test
    @DisplayName("Should handle repository exception during getEventConfigs")
    void getEventConfigs_RepositoryException_ThrowsException() {
        // Given
        when(repository.findAll())
                .thenThrow(new RuntimeException("Elasticsearch query failed"));

        // When & Then
        assertThatThrownBy(() -> service.getEventConfigs(null, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Elasticsearch query failed");

        verify(repository, times(1)).findAll();
        verify(mapper, never()).toResponseDto(any(EventConfigElastic.class));
    }

    @Test
    @DisplayName("Should handle null request DTO in createEventConfig")
    void createEventConfig_NullRequest_ThrowsException() {
        // Given
        when(mapper.toElasticEntity(null))
                .thenThrow(new RuntimeException("Request cannot be null"));

        // When & Then
        assertThatThrownBy(() -> service.createEventConfig(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Request cannot be null");

        verify(mapper, times(1)).toElasticEntity(null);
        verify(repository, never()).save(any(EventConfigElastic.class));
    }

    @Test
    @DisplayName("Should handle empty string ID in updateEventConfig")
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

    @Test
    @DisplayName("Should handle null ID in updateEventConfig")
    void updateEventConfig_NullId_ThrowsException() {
        // Given
        String nullId = null;
        when(repository.findById(nullId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.updateEventConfig(nullId, requestDto))
                .isInstanceOf(EventConfigNotFoundException.class)
                .hasMessage("Event config not found with id: " + nullId);

        verify(repository, times(1)).findById(nullId);
    }

    @Test
    @DisplayName("Should verify all filter combinations are properly tested")
    void getEventConfigs_AllFilterCombinations_Success() {
        // Test case 1: eventType + source + enabled = All 3 filters
        when(repository.findByEventTypeAndSourceAndEnabled("EVENT1", "source1", true))
                .thenReturn(Arrays.asList(elasticEntity));
        when(mapper.toResponseDto(elasticEntity)).thenReturn(responseDto);

        List<EventConfigResponseDto> result1 = service.getEventConfigs("EVENT1", "source1", true);
        assertThat(result1).hasSize(1);
        verify(repository).findByEventTypeAndSourceAndEnabled("EVENT1", "source1", true);

        // Test case 2: eventType + source (enabled = null) = 2 filters
        when(repository.findByEventTypeAndSource("EVENT2", "source2"))
                .thenReturn(Arrays.asList(elasticEntity));

        List<EventConfigResponseDto> result2 = service.getEventConfigs("EVENT2", "source2", null);
        assertThat(result2).hasSize(1);
        verify(repository).findByEventTypeAndSource("EVENT2", "source2");

        // Test case 3: eventType + enabled (source = null) = 2 filters
        when(repository.findByEventTypeAndEnabled("EVENT3", false))
                .thenReturn(Arrays.asList(elasticEntity));

        List<EventConfigResponseDto> result3 = service.getEventConfigs("EVENT3", null, false);
        assertThat(result3).hasSize(1);
        verify(repository).findByEventTypeAndEnabled("EVENT3", false);

        // Test case 4: source + enabled (eventType = null) = 2 filters
        when(repository.findBySourceAndEnabled("source4", true))
                .thenReturn(Arrays.asList(elasticEntity));

        List<EventConfigResponseDto> result4 = service.getEventConfigs(null, "source4", true);
        assertThat(result4).hasSize(1);
        verify(repository).findBySourceAndEnabled("source4", true);

        // Test case 5: eventType only = 1 filter
        when(repository.findByEventType("EVENT5"))
                .thenReturn(Arrays.asList(elasticEntity));

        List<EventConfigResponseDto> result5 = service.getEventConfigs("EVENT5", null, null);
        assertThat(result5).hasSize(1);
        verify(repository).findByEventType("EVENT5");

        // Test case 6: source only = 1 filter
        when(repository.findBySource("source6"))
                .thenReturn(Arrays.asList(elasticEntity));

        List<EventConfigResponseDto> result6 = service.getEventConfigs(null, "source6", null);
        assertThat(result6).hasSize(1);
        verify(repository).findBySource("source6");

        // Test case 7: enabled only = 1 filter
        when(repository.findByEnabled(false))
                .thenReturn(Arrays.asList(elasticEntity));

        List<EventConfigResponseDto> result7 = service.getEventConfigs(null, null, false);
        assertThat(result7).hasSize(1);
        verify(repository).findByEnabled(false);

        // Test case 8: no filters = findAll()
        when(repository.findAll()).thenReturn(Arrays.asList(elasticEntity));

        List<EventConfigResponseDto> result8 = service.getEventConfigs(null, null, null);
        assertThat(result8).hasSize(1);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should handle save exception with specific entity details")
    void updateEventConfig_SaveException_ThrowsException() {
        // Given
        String eventId = "test-uuid-123";
        EventConfigElastic existingEntity = EventConfigElastic.builder()
                .id(eventId)
                .eventType("USER_REGISTRATION")
                .source("auth-service")
                .enabled(true)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        when(repository.findById(eventId)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(EventConfigElastic.class)))
                .thenThrow(new RuntimeException("Save operation failed"));

        // When & Then
        assertThatThrownBy(() -> service.updateEventConfig(eventId, requestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Save operation failed");

        verify(repository, times(1)).findById(eventId);
        verify(repository, times(1)).save(existingEntity);
        verify(mapper, never()).toResponseDto(any(EventConfigElastic.class));
    }
}