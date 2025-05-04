package org.example.cloudstorage.service;

import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;

import java.util.List;

public interface DirectoryService {
    List<ResourceResponseDto> get(String path, User user);

    ResourceResponseDto create(String path, User user);
}
