package com.diplom.fileserver.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoMetadataDto {
    private final String name;
    private final String description;
    private final long views;
}
