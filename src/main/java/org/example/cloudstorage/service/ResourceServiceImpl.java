package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.MinioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.StreamSupport;

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStreamResource download(String path, User user) {
        String completePath = MINIO_USER_COMPLETE_PATH.formatted(user.getId(), path);

        try {
            return minioRepository.download(completePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return StreamSupport.stream(minioRepository.getListObjects(completePath, true).spliterator(), false)
                .map(ResourceResponseDtoMapper::toDto)
                .toList();
    }
}