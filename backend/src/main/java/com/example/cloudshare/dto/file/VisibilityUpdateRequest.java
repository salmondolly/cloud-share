package com.example.cloudshare.dto.file;

import com.example.cloudshare.model.FileVisibility;
import jakarta.validation.constraints.NotNull;

public record VisibilityUpdateRequest(@NotNull FileVisibility visibility) {
}
