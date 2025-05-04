package org.example.cloudstorage.service;

import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.exception.ResourceNotFoundMinioException;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.MinioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.example.cloudstorage.constant.AppConstants.MINIO_USER_COMPLETE_PATH;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final MinioRepository minioRepository;

    @Override
    public ResourceResponseDto get(String path, User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String path, User user) {
        String completePath = MINIO_USER_COMPLETE_PATH.formatted(user.getId(), path);
        try {
            minioRepository.delete(completePath);
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException("The path does not exist: %s".formatted(path), e);
        } catch (Exception e) {
            throw new MinioException("Error occurred while deleting object", e);
        }
    }

    @Override
    public InputStreamResource download(String path, User user) {
        String completePath = MINIO_USER_COMPLETE_PATH.formatted(user.getId(), path);
        try {
            return minioRepository.download(completePath);
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException("The path does not exist: %s".formatted(path), e);
        } catch (Exception e) {
            throw new MinioException("Error occurred while fetching list of objects", e);
        }
    }

    @Override
    public ResourceResponseDto move(String from, String to, User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ResourceResponseDto> search(String query, User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ResourceResponseDto> upload(String path, List<MultipartFile> file, User user) {
        String completePath = MINIO_USER_COMPLETE_PATH.formatted(user.getId(), path);
        try {
            minioRepository.upload(completePath, file);
            return minioRepository.getListObjects(completePath, true)
                    .stream()
                    .map(ResourceResponseDtoMapper::toDto)
                    .toList();
        } catch (Exception e) {
            throw new MinioException("Error occurred while uploading objects", e);
        }
    }
}