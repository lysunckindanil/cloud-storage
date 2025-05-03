package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.repo.MinioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final MinioRepository minioRepository;

    @Override
    public ResourceResponseDto get(String path, User user) {
        return null;
    }

    @Override
    public void delete(String path, User user) {

    }

    @Override
    public InputStreamResource download(String path, User user) {
        return null;
    }

    @Override
    public ResourceResponseDto move(String from, String to, User user) {
        return null;
    }

    @Override
    public List<ResourceResponseDto> search(String query, User user) {
        return List.of();
    }

    @Override
    public List<ResourceResponseDto> upload(String path, List<MultipartFile> file, User user) {
        return List.of();
    }
}