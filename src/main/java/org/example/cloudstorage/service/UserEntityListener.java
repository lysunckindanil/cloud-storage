package org.example.cloudstorage.service;

import jakarta.persistence.PostPersist;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.entity.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEntityListener {
    private final DirectoryService directoryService;

    @PostPersist
    public void createDirectoryForNewUser(User user) {
        directoryService.createDirectoryForNewUser(user);
    }
}
