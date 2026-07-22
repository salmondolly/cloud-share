package com.example.cloudshare.dto.file;

import com.example.cloudshare.model.FileVisibility;

import java.time.Instant;

public record FileResponse(
        String id,
        String originalFileName,
        long fileSize,
        String contentType,
        FileVisibility visibility,
        String shareToken,
        String shareUrl,
        Instant uploadedAt,
        long downloadCount
) {
}
