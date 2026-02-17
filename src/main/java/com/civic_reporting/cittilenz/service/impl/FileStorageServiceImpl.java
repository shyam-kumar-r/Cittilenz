package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public String storeFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File too large");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type");
        }

        try {

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadDir = Path.of("uploads");
            Files.createDirectories(uploadDir);

            Path filePath = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath);

            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("File storage failed", e);
        }
    }
}
