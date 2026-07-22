package com.example.cloudshare.controller;

import com.example.cloudshare.dto.file.FileResponse;
import com.example.cloudshare.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/share")
public class ShareController {
    private final FileService fileService;

    public ShareController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{token}")
    public FileResponse metadata(@PathVariable String token) {
        return fileService.getPublicMetadata(token);
    }

    @GetMapping("/{token}/download")
    public ResponseEntity<Resource> download(@PathVariable String token, HttpServletRequest request) {
        FileService.DownloadPayload payload = fileService.downloadPublic(token, clientAddress(request));
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(payload.contentType());
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(payload.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(payload.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(payload.resource());
    }

    private String clientAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
