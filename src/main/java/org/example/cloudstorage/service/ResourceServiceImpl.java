package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.exception.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.MinioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.example.cloudstorage.constant.AppConstants.MINIO_USER_PREFIX;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final MinioRepository minioRepository;

    @Override
    public ResourceResponseDto get(String path, User user) {
        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;
        return ResourceResponseDtoMapper.toDto(minioRepository.getByPath(completePath));
    }

    @Override
    public void delete(String path, User user) {
        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;
        minioRepository.delete(completePath);
    }

    @Override
    public InputStreamResource download(String path, User user) {
        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;
        return minioRepository.download(completePath);
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
    public List<ResourceResponseDto> upload(String path, List<MultipartFile> files, User user) {
        String completePath = MINIO_USER_PREFIX.formatted(user.getId()) + path;

        for (MultipartFile file : files) {
            if (minioRepository.existsByPath(completePath + "/" + file.getOriginalFilename()))
                throw new ResourceAlreadyExistsMinioException("File already exists: %s"
                        .formatted(file.getOriginalFilename()));
        }

        minioRepository.upload(completePath, files);
        return minioRepository.getList(completePath, true)
                .stream()
                .map(ResourceResponseDtoMapper::toDto)
                .toList();
    }
}