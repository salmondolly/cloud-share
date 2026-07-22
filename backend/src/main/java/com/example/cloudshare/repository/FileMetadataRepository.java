package com.example.cloudshare.repository;

import com.example.cloudshare.model.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    List<FileMetadata> findAllByOwnerIdOrderByUploadedAtDesc(String ownerId);
    Optional<FileMetadata> findByShareToken(String shareToken);
}
