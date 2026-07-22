package com.example.cloudshare.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "download_events")
public class DownloadEvent {
    @Id
    private String id;

    @Indexed
    private String fileId;

    private String userId;
    private DownloadSource source;
    private String remoteAddress;

    @CreatedDate
    private Instant occurredAt;

    public DownloadEvent() {
    }

    public DownloadEvent(String fileId, String userId, DownloadSource source, String remoteAddress) {
        this.fileId = fileId;
        this.userId = userId;
        this.source = source;
        this.remoteAddress = remoteAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public DownloadSource getSource() {
        return source;
    }

    public void setSource(DownloadSource source) {
        this.source = source;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
