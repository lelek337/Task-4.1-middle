package com.itm.space.repository;

import com.itm.space.BaseIntegrationTest;
import com.itm.space.model.entity.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

@Transactional
class FileRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private FileRepository fileRepository;

    private File file;

    @BeforeEach
    void setUp() {
        file = File.builder()
                .userId(UUID.randomUUID())
                .fileName("TEST_TEXT_FILE.txt")
                .fileData("Hello world".getBytes(StandardCharsets.UTF_8))
                .contentType("text")
                .uploadedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        file.setFileSize((long) file.getFileData().length);
    }

    @Test
    @DisplayName(value = "Создание сущности и получение ее по Id")
    void createFileAndGetById() {
        fileRepository.save(file);

        File savedFile = fileRepository.findById(file.getId())
                .orElseThrow(() -> new IllegalArgumentException("file not found"));

        assertThat(savedFile).isNotNull();
        assertThat(savedFile.getId()).isEqualTo(file.getId());
    }

    @Test
    @DisplayName(value = "Обновление сущности и получение ее по Id")
    void updateFileAndGetById() {
        fileRepository.save(file);
        file.setFileData("Any Hello world".getBytes(StandardCharsets.UTF_8));
        file.setFileSize((long) file.getFileData().length);
        fileRepository.save(file);

        File updatedFile = fileRepository.findById(file.getId())
                .orElseThrow(() -> new IllegalArgumentException("file not found"));

        assertThat(updatedFile).isNotNull();
        assertThat(updatedFile.getId()).isEqualTo(file.getId());
        assertThat(updatedFile.getFileData()).isEqualTo(file.getFileData());
        assertThat(updatedFile.getFileSize()).isEqualTo(file.getFileSize());
    }

    @Test
    @DisplayName("Удаляет только архивные данные с датой и менее cutoffDate")
    void deleteArchivedFile_deletesOnlyOldDeletedFiles() {
        LocalDateTime now = LocalDateTime.now();
        File oldDeletedFile = testFile("oldDeleted.txt", true, now.minusDays(100));
        File newDeletedFile = testFile("newDeleted.txt", true, now.minusDays(1));
        File oldActiveFile = testFile("oldActive.txt", true, now.minusDays(100));
        File newActiveFile = testFile("newActive.txt", true, now.minusDays(1));

        fileRepository.saveAll(Arrays.asList(oldDeletedFile, newDeletedFile, oldActiveFile, newActiveFile));

        int deletedCount = fileRepository.deletedArchivedFiles(now.minusDays(30));

        assertThat(deletedCount).isEqualTo(2);
        assertThat(fileRepository.findById(newDeletedFile.getId())).isPresent();
        assertThat(fileRepository.findById(oldActiveFile.getId())).isPresent();
        assertThat(fileRepository.findById(newActiveFile.getId())).isPresent();
    }

    @Test
    @DisplayName("Не удаляет файлы если нет подходящих по условию")
    void deleteArchivedFiles_nothingIfNoMatch() {
        LocalDateTime now = LocalDateTime.now();
        File file1 = testFile("file1.txt", false, now.minusDays(10));
        File file2 = testFile("file2.txt", false, now);

        fileRepository.save(file1);
        fileRepository.save(file2);

        int deletedCount = fileRepository.deletedArchivedFiles(now.minusDays(100));

        assertThat(deletedCount).isEqualTo(0);
        assertThat(fileRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Удаляет несколько файлов, если все подходят")
    void deleteArchivedFiles_deletesMultiple() {
        LocalDateTime now = LocalDateTime.now();
        File file1 = testFile("file.txt", true, now.minusDays(40));
        File file2 = testFile("file2.txt", true, now.minusDays(90));

        fileRepository.save(file1);
        fileRepository.save(file2);

        int deleteCount = fileRepository.deletedArchivedFiles(now.minusDays(30));
        assertThat(deleteCount).isEqualTo(2);
        assertThat(fileRepository.findAll()).isEmpty();
    }

    private File testFile(String name, boolean isDeleted, LocalDateTime deletedAt) {
        byte[] bytes = "test content".getBytes(StandardCharsets.UTF_8);

        return File.builder()
                .userId(UUID.randomUUID())
                .fileName(name)
                .contentType("application/octet-stream")
                .deletedAt(deletedAt)
                .fileData(bytes)
                .isDeleted(isDeleted)
                .uploadedAt(deletedAt != null ? deletedAt : LocalDateTime.now())
                .fileSize((long) bytes.length)
                .build();
    }
}
