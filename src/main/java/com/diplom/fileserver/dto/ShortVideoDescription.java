package com.diplom.fileserver.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortVideoDescription {
    private final String id;
    private final String name;
}
