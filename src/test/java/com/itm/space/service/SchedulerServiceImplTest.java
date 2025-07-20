package com.itm.space.service;

import com.itm.space.BaseUnitTest;
import com.itm.space.repository.FileRepository;
import com.itm.space.service.impl.SchedulerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulerServiceImplTest extends BaseUnitTest {

    @Mock
    private FileRepository fileRepository;

    @Captor
    ArgumentCaptor<LocalDateTime> cutoffCaptor;

    private SchedulerServiceImpl schedulerService;

    @BeforeEach
    void setUp() {
        schedulerService = new SchedulerServiceImpl(fileRepository);
    }

    @Test
    @DisplayName("Должен успешно удалить архивные файлы")
    void shouldDeletedArchivedFiles() {
        // Given
        int retentionDays = 30;
        int expectedDeletedCount = 5;
        when(fileRepository.deletedArchivedFiles(any(LocalDateTime.class)))
                .thenReturn(expectedDeletedCount);

        // When
        schedulerService.cleanupArchivedFiles(retentionDays);

        // Then
        verify(fileRepository, times(1)).deletedArchivedFiles(any(LocalDateTime.class));

        verify(fileRepository).deletedArchivedFiles(argThat(cutoff -> {
            LocalDateTime expectedCutoff = LocalDateTime.now().minusDays(retentionDays);
            return cutoff.isBefore(expectedCutoff.plusSeconds(1)) &&
                    cutoff.isAfter(expectedCutoff.minusSeconds(1));
        }));
    }

    @Test
    @DisplayName("Должен обработать случай когда нет файлов для удаления")
    void shouldDeleteZeroFiles() {
        // Given
        int retentionDays = 7;
        int expectedDeletedCount = 0;
        when(fileRepository.deletedArchivedFiles(any(LocalDateTime.class)))
                .thenReturn(expectedDeletedCount);

        // When
        schedulerService.cleanupArchivedFiles(retentionDays);

        // Then
        verify(fileRepository, times(1)).deletedArchivedFiles(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать усключение из рпозитория")
    void shouldHandleRepositoryException() {
        // Given
        int retentionDays = 14;
        when(fileRepository.deletedArchivedFiles(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Ошибка подключения к базе данных"));

        // When
        schedulerService.cleanupArchivedFiles(retentionDays);

        // Then
        verify(fileRepository, times(1)).deletedArchivedFiles(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Должен обработать нулевой период хранения")
    void shouldHandleZeroRetentionDays() {
        // Given
        int retentionDays = 0;
        when(fileRepository.deletedArchivedFiles(any(LocalDateTime.class)))
                .thenReturn(10);

        // When
        schedulerService.cleanupArchivedFiles(retentionDays);

        // Then
        verify(fileRepository).deletedArchivedFiles(argThat(cutoff -> {
            LocalDateTime expectedCutoff = LocalDateTime.now();
            return cutoff.isBefore(expectedCutoff.plusSeconds(1)) &&
                    cutoff.isAfter(expectedCutoff.minusSeconds(1));
        }));
    }

    @Test
    @DisplayName("Должен обработать отрицательный период хранения")
    void shouldHandleNegativeRetentionDays() {
        // Given
        int retentionDays = -5;
        when(fileRepository.deletedArchivedFiles(any(LocalDateTime.class)))
                .thenReturn(0);

        // When
        schedulerService.cleanupArchivedFiles(retentionDays);

        // Then
        verify(fileRepository).deletedArchivedFiles(argThat(cutoff -> {
            LocalDateTime expectedCutoff = LocalDateTime.now().minusDays(retentionDays);
            return cutoff.isBefore(expectedCutoff.plusSeconds(1)) &&
                    cutoff.isAfter(expectedCutoff.minusSeconds(1));
        }));
    }

    @Test
    @DisplayName("Должен передать правильную дату отсечки")
    void shouldPassCorrectCutoffDate() {
        // Given
        int retentionDays = 30;
        when(fileRepository.deletedArchivedFiles(any(LocalDateTime.class)))
                .thenReturn(5);

        // When
        schedulerService.cleanupArchivedFiles(retentionDays);

        // Then
        verify(fileRepository).deletedArchivedFiles(cutoffCaptor.capture());
        LocalDateTime capturedCutoff = cutoffCaptor.getValue();

        LocalDateTime expectedCutoff = LocalDateTime.now().minusDays(retentionDays);
        assertTrue(capturedCutoff.isBefore(expectedCutoff.plusSeconds(1)));
        assertTrue(capturedCutoff.isAfter(expectedCutoff.minusSeconds(1)));
    }
}
