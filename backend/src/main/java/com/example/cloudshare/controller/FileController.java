package com.example.cloudshare.controller;

import com.example.cloudshare.dto.file.FileResponse;
import com.example.cloudshare.dto.file.VisibilityUpdateRequest;
import com.example.cloudshare.model.FileVisibility;
import com.example.cloudshare.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(
            Authentication authentication,
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "PRIVATE") FileVisibility visibility
    ) {
        return ResponseEntity.status(201).body(fileService.upload(authentication.getName(), file, visibility));
    }

    @GetMapping
    public List<FileResponse> list(Authentication authentication) {
        return fileService.listOwnedFiles(authentication.getName());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            Authentication authentication,
            @PathVariable String id,
            HttpServletRequest request
    ) {
        FileService.DownloadPayload payload = fileService.downloadOwned(
                authentication.getName(), id, clientAddress(request));
        return downloadResponse(payload);
    }

    @PatchMapping("/{id}/visibility")
    public FileResponse updateVisibility(
            Authentication authentication,
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody VisibilityUpdateRequest request
    ) {
        return fileService.updateVisibility(authentication.getName(), id, request.visibility());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable String id) {
        fileService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> downloadResponse(FileService.DownloadPayload payload) {
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
