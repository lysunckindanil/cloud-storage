package org.example.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import org.example.cloudstorage.model.ResourceType;

@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceResponseDto {
    String path;
    String name;
    Long size;
    ResourceType type;
}