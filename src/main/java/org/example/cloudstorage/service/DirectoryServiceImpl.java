package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.MinioManagementFacade;
import org.example.cloudstorage.util.MinioUserPathUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final MinioManagementFacade minioRepository;

    @Override
    public List<ResourceResponseDto> get(String path, User user) {
        String completePath = MinioUserPathUtils.constructPath(path, user);
        return minioRepository.listFiles(completePath, false)
                .stream()
                .map(ResourceResponseDtoMapper::toDto)
                .toList();
    }

    @Override
    public ResourceResponseDto create(String path, User user) {
        String completePath = MinioUserPathUtils.constructPath(path, user);
        return ResourceResponseDtoMapper.toDto(minioRepository.createEmptyDirectory(completePath));
    }

    @Override
    public void createDirectoryForNewUser(User user) {
        minioRepository.createEmptyDirectory(MinioUserPathUtils.constructPath("", user));
    }
}
