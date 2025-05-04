package org.example.cloudstorage.mapper;

import io.minio.Result;
import io.minio.messages.Item;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.model.ResourceType;
import org.example.cloudstorage.util.PathUtils;

public class ResourceResponseDtoMapper {
    public static ResourceResponseDto toDto(Result<Item> resultItem) {
        try {
            Item item = resultItem.get();

            PathUtils.Breadcrumb breadcrumb = PathUtils.constructBreadcrumb(item.objectName(), item.isDir(), 1);

            return ResourceResponseDto
                    .builder()
                    .name(breadcrumb.getName())
                    .path(breadcrumb.getPath())
                    .type(item.isDir() ? ResourceType.DIRECTORY : ResourceType.FILE)
                    .size(item.isDir() ? null : item.size())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResourceResponseDto toDto(String path) {
        PathUtils.Breadcrumb breadcrumb = PathUtils.constructBreadcrumb(path, true, 1);
        return ResourceResponseDto
                .builder()
                .name(breadcrumb.getName())
                .path(breadcrumb.getPath())
                .type(ResourceType.DIRECTORY)
                .build();
    }
}
