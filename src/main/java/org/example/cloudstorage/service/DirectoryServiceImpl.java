package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.repo.MinioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {
    private final MinioRepository minioRepository;

    @Override
    public List<ResourceResponseDto> get(String path, User userDetails) {
        return null;
    }

    @Override
    public ResourceResponseDto create(String path, User user) {
        return null;
    }
}
