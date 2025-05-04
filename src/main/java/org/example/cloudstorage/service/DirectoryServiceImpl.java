package org.example.cloudstorage.service;

import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.exception.ResourceNotFoundMinioException;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.MinioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.cloudstorage.constant.AppConstants.MINIO_USER_COMPLETE_PATH;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final MinioRepository minioRepository;

    @Override
    public List<ResourceResponseDto> get(String path, User user) {
        String completePath = MINIO_USER_COMPLETE_PATH.formatted(user.getId(), path);
        try {
            return minioRepository.getListObjects(completePath, false)
                    .stream()
                    .map(ResourceResponseDtoMapper::toDto)
                    .toList();
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException("The path does not exist: %s".formatted(path), e);
        } catch (Exception e) {
            throw new MinioException("Error occurred while fetching list of objects", e);
        }
    }

    @Override
    public ResourceResponseDto create(String path, User user) {
        String completePath = MINIO_USER_COMPLETE_PATH.formatted(user.getId(), path);
        try {
            minioRepository.createEmptyDirectory(completePath);
        } catch (Exception e) {
            throw new MinioException("Cannot create empty directory", e);
        }
        return ResourceResponseDtoMapper.toDto(completePath);
    }
}
