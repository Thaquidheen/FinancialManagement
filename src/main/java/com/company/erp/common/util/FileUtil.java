package com.company.erp.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Utility class for file operations
 */
public class FileUtil {

    private FileUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Save byte array to file
     */
    public static void saveFile(byte[] data, String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // Create parent directories if they don't exist
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Save text content to file
     */
    public static void saveTextFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // Create parent directories if they don't exist
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Read file content as string
     */
    public static String readTextFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path);
    }

    /**
     * Read file content as byte array
     */
    public static byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    /**
     * Check if file exists
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Delete file if exists
     */
    public static boolean deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.deleteIfExists(path);
    }

    /**
     * Get file size in bytes
     */
    public static long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.size(path);
    }

    /**
     * Create directory if it doesn't exist
     */
    public static void createDirectories(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        Files.createDirectories(path);
    }
}
