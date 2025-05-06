package org.example.cloudstorage.mapper;

import io.minio.messages.Item;
import jakarta.annotation.Resource;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.model.ResourceType;
import org.example.cloudstorage.util.PathUtils;

public class ResourceResponseDtoMapper {
    public static ResourceResponseDto toDto(Item item) {
        PathUtils.Breadcrumb breadcrumb = PathUtils.constructBreadcrumb(item.objectName(), item.isDir(), 1);

        return ResourceResponseDto
                .builder()
                .name(breadcrumb.getName())
                .path(breadcrumb.getPath())
                .type(item.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE)
                .size(item.isDir() ? null : item.size())
                .build();
    }

    public static ResourceResponseDto toDto(String path) {
        PathUtils.Breadcrumb breadcrumb = PathUtils.constructBreadcrumb(path.substring(0, path.length() - 2), true, 1);
        return ResourceResponseDto
                .builder()
                .name(breadcrumb.getName())
                .path(breadcrumb.getPath())
                .type(ResourceType.DIRECTORY)
                .build();
    }

    public static ResourceResponseDto toDto(String path, Long size) {
        PathUtils.Breadcrumb breadcrumb = PathUtils.constructBreadcrumb(path, false, 1);
        return ResourceResponseDto
                .builder()
                .name(breadcrumb.getName())
                .path(breadcrumb.getPath())
                .size(size)
                .type(ResourceType.FILE)
                .build();
    }
}
