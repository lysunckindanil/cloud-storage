package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.mapper.ResourceResponseDtoMapper;
import org.example.cloudstorage.minio.HierarchicalMinioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.example.cloudstorage.util.MinioUserPathUtils.constructPath;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final HierarchicalMinioRepository minioRepository;

    @Override
    public ResourceResponseDto get(String path, User user) {
        return ResourceResponseDtoMapper.toDto(
                minioRepository.getResource(constructPath(path, user))
        );
    }

    @Override
    public void delete(String path, User user) {
        minioRepository.delete(constructPath(path, user));
    }

    @Override
    public InputStreamResource download(String path, User user) {
        return minioRepository.download(constructPath(path, user));
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
        String completePath = constructPath(path, user);
        minioRepository.upload(completePath, files);

        return minioRepository.listResources(completePath, true)
                .stream()
                .map(ResourceResponseDtoMapper::toDto)
                .toList();
    }
}