package com.example.cloudshare.repository;

import com.example.cloudshare.model.DownloadEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DownloadEventRepository extends MongoRepository<DownloadEvent, String> {
}
