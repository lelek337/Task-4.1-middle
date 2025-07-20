package com.itm.space.scheduler;

import com.itm.space.BaseIntegrationTest;
import com.itm.space.model.entity.File;
import com.itm.space.repository.FileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(properties = {
        "scheduler.cleanup.cron=0/2 * * * * *",
        "scheduler.cleanup.retention-days=30"
})
public class CleanupSchedulerTest extends BaseIntegrationTest {

    @Autowired
    private CleanupScheduler cleanupScheduler;

    @Autowired
    private FileRepository repository;

    @Test
    @Transactional
    @DisplayName("Должен очищать архивные файлы при запуске планеровщика")
    void shouldCleanupArchivedFilesWhenSchedulerRuns() {
        // Given
        LocalDateTime oldDate = LocalDateTime.now().minusDays(35);
        LocalDateTime recentDate = LocalDateTime.now().minusDays(10);

        File oldFile1 = createTestFile("old_file.pdf", oldDate, true, oldDate);
        File oldFile2 = createTestFile("old_file2.jpg", oldDate, true, oldDate);

        File recentFile = createTestFile("recent_file.txt", recentDate, true, recentDate);
        File activeFile = createTestFile("active_file.docx", LocalDateTime.now(), false, null);

        repository.saveAll(Arrays.asList(oldFile1, oldFile2, recentFile, activeFile));
        long initCount = repository.count();
        assertEquals(4, initCount);

        // When
        cleanupScheduler.cleanup();

        // Then
        long finalCount = repository.count();
        assertEquals(2, finalCount);

        List<File> remainingFiles = repository.findAll();
        assertThat(remainingFiles)
                .hasSize(2)
                .extracting(File::getFileName)
                .containsExactlyInAnyOrder("recent_file.txt", "active_file.docx");
    }

    @Test
    @DisplayName("Должен корректно отрабатывать пустой репозиторий")
    void shouldHandleEmptyRepositoryGracefully() {
        // Given
        repository.deleteAll();

        // When & Then
        assertDoesNotThrow(() -> cleanupScheduler.cleanup());
        assertEquals(0, repository.count());
    }

    @Test
    @Transactional
    @DisplayName("Не должен падать когда нет файлов для отчистки")
    void shouldNotFailWhenNoFilesMatchCleanupCriteria() {
        // Given
        File recentFile = createTestFile("recent_file.txt", LocalDateTime.now(), true, LocalDateTime.now());
        File activeFile = createTestFile("active_file.docx", LocalDateTime.now(), false, null);

        repository.save(recentFile);
        repository.save(activeFile);

        // When & Then
        assertDoesNotThrow(() -> cleanupScheduler.cleanup());
        assertEquals(2, repository.count());
    }

    private File createTestFile(String fileName, LocalDateTime uploadedAt, boolean isDeleted, LocalDateTime deletedAt) {
        byte[] bytes = "test content".getBytes(StandardCharsets.UTF_8);
        return File.builder()
                .contentType("application/octet-stream")
                .deletedAt(deletedAt)
                .fileData(bytes)
                .fileName(fileName)
                .isDeleted(isDeleted)
                .uploadedAt(uploadedAt)
                .userId(UUID.randomUUID())
                .fileSize((long) bytes.length)
                .build();
    }
}
