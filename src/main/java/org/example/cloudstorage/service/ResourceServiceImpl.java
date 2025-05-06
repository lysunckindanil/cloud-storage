package org.example.cloudstorage.service;

import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.exception.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.exception.ResourceNotFoundMinioException;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.MinioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.example.cloudstorage.constant.AppConstants.FOLDER_PREFIX;
import static org.example.cloudstorage.constant.AppConstants.MINIO_USER_PREFIX;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final MinioRepository minioRepository;

    @Override
    public ResourceResponseDto get(String path, User user) {
        boolean lookingForDir = path.endsWith("/") || path.isEmpty();
        if (path.startsWith("/")) path = path.substring(1);

        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;
        if (lookingForDir) {
            completePath += FOLDER_PREFIX;
        }

        try {
            StatObjectResponse object = minioRepository.getObject(completePath);
            if (lookingForDir) {
                return ResourceResponseDtoMapper.toDto(completePath);
            }
            return ResourceResponseDtoMapper.toDto(completePath, object.size());
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException("The path does not exist: %s".formatted(path), e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    @Override
    public void delete(String path, User user) {
        if (path.startsWith("/")) path = path.substring(1);
        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;
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
        if (path.startsWith("/")) path = path.substring(1);
        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;
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
        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;

        try {
            for (MultipartFile fileItem : file) {
                if (minioRepository.existsByPath(completePath + "/" + fileItem.getOriginalFilename()))
                    throw new ResourceAlreadyExistsMinioException("File already exists: %s".formatted(fileItem.getOriginalFilename()));
            }

            minioRepository.upload(completePath, file);
            return minioRepository.getList(completePath, true)
                    .stream()
                    .map(ResourceResponseDtoMapper::toDto)
                    .toList();
        } catch (Exception e) {
            throw new MinioException("Error occurred while uploading objects", e);
        }
    }
}