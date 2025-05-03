package org.example.cloudstorage.service;

import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    ResourceResponseDto get(String path, User user);

    void delete(String path, User user);

    InputStreamResource download(String path, User user);

    ResourceResponseDto move(String from, String to, User user);

    List<ResourceResponseDto> search(String query, User user);

    List<ResourceResponseDto> upload(String path, List<MultipartFile> file, User user);
}
