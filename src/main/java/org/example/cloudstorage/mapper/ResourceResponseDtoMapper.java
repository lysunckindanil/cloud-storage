package org.example.cloudstorage.mapper;

import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.model.ResourceMetadata;
import org.example.cloudstorage.model.ResourceType;
import org.example.cloudstorage.util.PathUtils;

public class ResourceResponseDtoMapper {
    public static ResourceResponseDto toDto(ResourceMetadata object) {
        PathUtils.Breadcrumb breadcrumb = PathUtils.constructBreadcrumb(object.name(), object.isDirectory(), 1);

        return ResourceResponseDto
                .builder()
                .name(breadcrumb.getName())
                .path(breadcrumb.getPath())
                .type(object.isDirectory() ? ResourceType.DIRECTORY : ResourceType.FILE)
                .size(object.isDirectory() ? null : object.size())
                .build();
    }
}
