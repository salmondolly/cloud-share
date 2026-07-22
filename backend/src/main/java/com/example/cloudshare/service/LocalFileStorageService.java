package com.example.cloudshare.service;

import com.example.cloudshare.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path uploadRoot;

    public LocalFileStorageService(@Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new FileStorageException("Could not create the upload directory", exception);
        }
    }

    @Override
    public String store(MultipartFile file) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String storedFileName = UUID.randomUUID() + extension;
        Path target = safeResolve(storedFileName);

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException exception) {
            throw new FileStorageException("Could not store the uploaded file", exception);
        }
    }

    @Override
    public Resource load(String storedFileName) {
        try {
            Path file = safeResolve(storedFileName);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("Stored file is missing or unreadable");
            }
            return resource;
        } catch (IOException exception) {
            throw new FileStorageException("Could not read the stored file", exception);
        }
    }

    @Override
    public void delete(String storedFileName) {
        try {
            Files.deleteIfExists(safeResolve(storedFileName));
        } catch (IOException exception) {
            throw new FileStorageException("Could not delete the stored file", exception);
        }
    }

    private Path safeResolve(String storedFileName) {
        Path resolved = uploadRoot.resolve(storedFileName).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new FileStorageException("Invalid storage path");
        }
        return resolved;
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        String extension = fileName.substring(dot).toLowerCase(Locale.ROOT);
        return extension.length() <= 16 ? extension : "";
    }
}
