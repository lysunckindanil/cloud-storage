package org.example.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import org.example.cloudstorage.model.Type;

@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceResponseDto {
    String path;
    String name;
    Long size;
    Type type;
}