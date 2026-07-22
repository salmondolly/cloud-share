package com.example.cloudshare.service;

import com.example.cloudshare.dto.file.FileResponse;
import com.example.cloudshare.exception.ResourceNotFoundException;
import com.example.cloudshare.exception.UnauthorizedOperationException;
import com.example.cloudshare.model.DownloadEvent;
import com.example.cloudshare.model.DownloadSource;
import com.example.cloudshare.model.FileMetadata;
import com.example.cloudshare.model.FileVisibility;
import com.example.cloudshare.model.User;
import com.example.cloudshare.repository.DownloadEventRepository;
import com.example.cloudshare.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    private final FileMetadataRepository fileRepository;
    private final DownloadEventRepository downloadEventRepository;
    private final FileStorageService fileStorageService;
    private final FileValidationService fileValidationService;
    private final UserService userService;
    private final PlanPolicyService planPolicyService;
    private final MongoTemplate mongoTemplate;
    private final String frontendBaseUrl;

    public FileService(
            FileMetadataRepository fileRepository,
            DownloadEventRepository downloadEventRepository,
            FileStorageService fileStorageService,
            FileValidationService fileValidationService,
            UserService userService,
            PlanPolicyService planPolicyService,
            MongoTemplate mongoTemplate,
            @Value("${app.frontend-base-url:http://localhost:5173}") String frontendBaseUrl
    ) {
        this.fileRepository = fileRepository;
        this.downloadEventRepository = downloadEventRepository;
        this.fileStorageService = fileStorageService;
        this.fileValidationService = fileValidationService;
        this.userService = userService;
        this.planPolicyService = planPolicyService;
        this.mongoTemplate = mongoTemplate;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/$", "");
    }

    public FileResponse upload(String email, MultipartFile file, FileVisibility visibility) {
        User user = userService.findByEmail(email);
        PlanPolicy policy = planPolicyService.getPolicy(user.getPlanType());
        fileValidationService.validate(file, policy.maxFileSizeBytes());

        userService.consumeUploadCredit(user.getId());
        String storedFileName = null;
        try {
            storedFileName = fileStorageService.store(file);
            FileMetadata metadata = new FileMetadata();
            metadata.setStoredFileName(storedFileName);
            metadata.setOriginalFileName(fileValidationService.safeOriginalName(file));
            metadata.setFileSize(file.getSize());
            metadata.setContentType(StringUtils.hasText(file.getContentType())
                    ? file.getContentType()
                    : "application/octet-stream");
            metadata.setOwnerId(user.getId());
            metadata.setVisibility(visibility == null ? FileVisibility.PRIVATE : visibility);
            metadata.setShareToken(metadata.getVisibility() == FileVisibility.PUBLIC ? newShareToken() : null);
            metadata.setUploadedAt(Instant.now());
            metadata.setDownloadCount(0);
            return toResponse(fileRepository.save(metadata));
        } catch (RuntimeException exception) {
            if (storedFileName != null) {
                try {
                    fileStorageService.delete(storedFileName);
                } catch (RuntimeException ignored) {
                    // The original exception remains the useful failure to return.
                }
            }
            userService.refundUploadCredit(user.getId());
            throw exception;
        }
    }

    public List<FileResponse> listOwnedFiles(String email) {
        User user = userService.findByEmail(email);
        return fileRepository.findAllByOwnerIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FileResponse updateVisibility(String email, String fileId, FileVisibility visibility) {
        User user = userService.findByEmail(email);
        FileMetadata metadata = ownedFile(user.getId(), fileId);
        metadata.setVisibility(visibility);
        metadata.setShareToken(visibility == FileVisibility.PUBLIC
                ? (metadata.getShareToken() == null ? newShareToken() : metadata.getShareToken())
                : null);
        return toResponse(fileRepository.save(metadata));
    }

    public void delete(String email, String fileId) {
        User user = userService.findByEmail(email);
        FileMetadata metadata = ownedFile(user.getId(), fileId);
        fileStorageService.delete(metadata.getStoredFileName());
        fileRepository.delete(metadata);
    }

    public DownloadPayload downloadOwned(String email, String fileId, String remoteAddress) {
        User user = userService.findByEmail(email);
        FileMetadata metadata = ownedFile(user.getId(), fileId);
        registerDownload(metadata, user.getId(), DownloadSource.OWNER, remoteAddress);
        return payload(metadata);
    }

    public FileResponse getPublicMetadata(String token) {
        return toResponse(publicFile(token));
    }

    public DownloadPayload downloadPublic(String token, String remoteAddress) {
        FileMetadata metadata = publicFile(token);
        registerDownload(metadata, null, DownloadSource.PUBLIC_LINK, remoteAddress);
        return payload(metadata);
    }

    private FileMetadata publicFile(String token) {
        FileMetadata metadata = fileRepository.findByShareToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Shared file not found"));
        if (metadata.getVisibility() != FileVisibility.PUBLIC) {
            throw new ResourceNotFoundException("Shared file not found");
        }
        return metadata;
    }

    private FileMetadata ownedFile(String userId, String fileId) {
        FileMetadata metadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        if (!metadata.getOwnerId().equals(userId)) {
            throw new UnauthorizedOperationException("You do not have permission to access this file");
        }
        return metadata;
    }

    private DownloadPayload payload(FileMetadata metadata) {
        Resource resource = fileStorageService.load(metadata.getStoredFileName());
        return new DownloadPayload(resource, metadata.getOriginalFileName(), metadata.getContentType(), metadata.getFileSize());
    }

    private void registerDownload(FileMetadata metadata, String userId, DownloadSource source, String remoteAddress) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(metadata.getId())),
                new Update().inc("downloadCount", 1),
                FileMetadata.class
        );
        downloadEventRepository.save(new DownloadEvent(metadata.getId(), userId, source, remoteAddress));
    }

    private FileResponse toResponse(FileMetadata metadata) {
        String shareUrl = metadata.getShareToken() == null
                ? null
                : frontendBaseUrl + "/share/" + metadata.getShareToken();
        return new FileResponse(
                metadata.getId(),
                metadata.getOriginalFileName(),
                metadata.getFileSize(),
                metadata.getContentType(),
                metadata.getVisibility(),
                metadata.getShareToken(),
                shareUrl,
                metadata.getUploadedAt(),
                metadata.getDownloadCount()
        );
    }

    private String newShareToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public record DownloadPayload(Resource resource, String fileName, String contentType, long fileSize) {
    }
}
