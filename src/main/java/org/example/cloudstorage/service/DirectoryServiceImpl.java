package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.MinioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.example.cloudstorage.constant.AppConstants.MINIO_USER_COMPLETE_PATH;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final MinioRepository minioRepository;


    @Override
    public List<ResourceResponseDto> get(String path, User user) {
        String completePath = MINIO_USER_COMPLETE_PATH.formatted(user.getId(), path);

        return StreamSupport
                .stream(minioRepository.getListObjects(completePath, false).spliterator(), false)
                .map(ResourceResponseDtoMapper::toDto)
                .toList();
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
