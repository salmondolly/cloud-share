package com.example.cloudshare.service;

import com.example.cloudshare.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

@Service
public class FileValidationService {
    private static final int MAX_FILE_NAME_LENGTH = 255;
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "msi", "bat", "cmd", "com", "scr", "ps1", "vbs", "jar", "war", "class"
    );

    public void validate(MultipartFile file, long maximumSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Choose a non-empty file to upload");
        }
        if (file.getSize() > maximumSizeBytes) {
            throw new InvalidFileException("This file exceeds your plan's upload limit");
        }

        String safeName = safeOriginalName(file);
        if (safeName.length() > MAX_FILE_NAME_LENGTH) {
            throw new InvalidFileException("File name is too long");
        }
        if (safeName.contains("\u0000")) {
            throw new InvalidFileException("File name contains invalid characters");
        }

        String extension = extensionOf(safeName);
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("Executable files are not permitted in this version");
        }
    }

    public String safeOriginalName(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            return "unnamed-file";
        }
        String fileName = Path.of(StringUtils.cleanPath(original)).getFileName().toString();
        if (fileName.equals(".") || fileName.equals("..")) {
            throw new InvalidFileException("Invalid file name");
        }
        return fileName;
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 && dot < fileName.length() - 1
                ? fileName.substring(dot + 1).toLowerCase(Locale.ROOT)
                : "";
    }
}
