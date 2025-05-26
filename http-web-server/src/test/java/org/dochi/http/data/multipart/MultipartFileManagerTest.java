package org.dochi.http.data.multipart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

class MultipartFileManagerTest {

    private MultipartFileManager fileManager;
    private UUID testUuid;
    private String testFileName;
    private byte[] testContent;

    @BeforeEach
    void setUp() throws Exception {
        fileManager = MultipartFileManager.getInstance();
        testUuid = UUID.randomUUID();
        testFileName = "testfile";
        testContent = "Hello, World! This is test content.".getBytes();

        // 테스트 전 임시 디렉토리 정리
        cleanupTestDirectory();
    }

    @AfterEach
    void tearDown() throws Exception {
        // 테스트 후 정리
        cleanupTestDirectory();
    }

    private void cleanupTestDirectory() throws Exception {
        File rootDir = new File("multipart-tmp-file");
        if (rootDir.exists()) {
            File[] files = rootDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Test
    void singletonInstance() {
        MultipartFileManager instance1 = MultipartFileManager.getInstance();
        MultipartFileManager instance2 = MultipartFileManager.getInstance();

        assertSame(instance1, instance2);
        assertNotNull(instance1);
    }

    @Test
    void rootDirectoryCreation() {
        MultipartFileManager.getInstance();

        File rootDirectory = new File("multipart-tmp-file");
        assertTrue(rootDirectory.exists());
        assertTrue(rootDirectory.isDirectory());
    }

    @Test
    void uploadFileSuccess() throws IOException {
        assertDoesNotThrow(() -> fileManager.uploadFile(testFileName, testUuid, testContent));

        String expectedPath = "multipart-tmp-file/" + testFileName + "_" + testUuid.toString() + ".bin";
        File uploadedFile = new File(expectedPath);
        assertTrue(uploadedFile.exists());
        assertTrue(uploadedFile.isFile());

        byte[] actualContent = Files.readAllBytes(uploadedFile.toPath());
        assertArrayEquals(testContent, actualContent);
    }

    @Test
    void uploadEmptyFile() throws IOException {
        byte[] emptyContent = new byte[0];
        fileManager.uploadFile(testFileName, testUuid, emptyContent);

        String expectedPath = "multipart-tmp-file/" + testFileName + "_" + testUuid.toString() + ".bin";
        File uploadedFile = new File(expectedPath);
        assertTrue(uploadedFile.exists());
        assertEquals(0, uploadedFile.length());
    }

    @Test
    void uploadLargeFile() throws IOException {
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        fileManager.uploadFile(testFileName, testUuid, largeContent);

        String expectedPath = "multipart-tmp-file/" + testFileName + "_" + testUuid.toString() + ".bin";
        File uploadedFile = new File(expectedPath);
        assertTrue(uploadedFile.exists());
        assertEquals(largeContent.length, uploadedFile.length());

        byte[] actualContent = Files.readAllBytes(uploadedFile.toPath());
        assertArrayEquals(largeContent, actualContent);
    }

    @Test
    void uploadSameFileNameDifferentUuid() throws IOException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        byte[] content1 = "Content 1".getBytes();
        byte[] content2 = "Content 2".getBytes();

        fileManager.uploadFile(testFileName, uuid1, content1);
        fileManager.uploadFile(testFileName, uuid2, content2);

        String path1 = "multipart-tmp-file/" + testFileName + "_" + uuid1.toString() + ".bin";
        String path2 = "multipart-tmp-file/" + testFileName + "_" + uuid2.toString() + ".bin";

        File file1 = new File(path1);
        File file2 = new File(path2);

        assertTrue(file1.exists());
        assertTrue(file2.exists());

        assertArrayEquals(content1, Files.readAllBytes(file1.toPath()));
        assertArrayEquals(content2, Files.readAllBytes(file2.toPath()));
    }

    @Test
    void uploadOverwriteExistingFile() throws IOException {
        byte[] originalContent = "Original content".getBytes();
        byte[] newContent = "New content".getBytes();

        fileManager.uploadFile(testFileName, testUuid, originalContent);
        fileManager.uploadFile(testFileName, testUuid, newContent);

        String expectedPath = "multipart-tmp-file/" + testFileName + "_" + testUuid.toString() + ".bin";
        File uploadedFile = new File(expectedPath);
        byte[] actualContent = Files.readAllBytes(uploadedFile.toPath());
        assertArrayEquals(newContent, actualContent);
    }

    @Test
    void downloadFileSuccess() throws IOException {
        fileManager.uploadFile(testFileName, testUuid, testContent);

        byte[] downloadedContent = fileManager.downloadFile(testFileName, testUuid);

        assertArrayEquals(testContent, downloadedContent);
    }

    @Test
    void downloadEmptyFile() throws IOException {
        byte[] emptyContent = new byte[0];
        fileManager.uploadFile(testFileName, testUuid, emptyContent);

        byte[] downloadedContent = fileManager.downloadFile(testFileName, testUuid);

        assertArrayEquals(emptyContent, downloadedContent);
        assertEquals(0, downloadedContent.length);
    }

    @Test
    void downloadNonExistentFile() {
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> fileManager.downloadFile("nonexistent", testUuid)
        );
        assertTrue(exception.getMessage().contains("File not found: nonexistent"));
    }

    @Test
    void downloadLargeFile() throws IOException {
        byte[] largeContent = new byte[1024 * 1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        fileManager.uploadFile(testFileName, testUuid, largeContent);

        byte[] downloadedContent = fileManager.downloadFile(testFileName, testUuid);

        assertArrayEquals(largeContent, downloadedContent);
    }

    @Test
    void deleteFileSuccess() throws IOException {
        fileManager.uploadFile(testFileName, testUuid, testContent);
        String expectedPath = "multipart-tmp-file/" + testFileName + "_" + testUuid.toString() + ".bin";
        File file = new File(expectedPath);
        assertTrue(file.exists());

        fileManager.deleteFile(testFileName, testUuid);

        assertFalse(file.exists());
    }

    @Test
    void deleteNonExistentFile() {
        FileNotFoundException exception = assertThrows(
                FileNotFoundException.class,
                () -> fileManager.deleteFile("nonexistent", testUuid)
        );
        assertTrue(exception.getMessage().contains("File not found: nonexistent"));
    }

    @Test
    void downloadAfterDelete() throws IOException {
        fileManager.uploadFile(testFileName, testUuid, testContent);
        fileManager.deleteFile(testFileName, testUuid);

        assertThrows(
                FileNotFoundException.class,
                () -> fileManager.downloadFile(testFileName, testUuid)
        );
    }

    @Test
    void uploadWithNullFileName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileManager.uploadFile(null, testUuid, testContent)
        );
        assertEquals("File name is null or empty", exception.getMessage());
    }

    @Test
    void uploadWithEmptyFileName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileManager.uploadFile("", testUuid, testContent)
        );
        assertEquals("File name is null or empty", exception.getMessage());
    }
}
