package org.example.cloudstorage.service;

import jakarta.persistence.PostPersist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.entity.User;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEntityListener {
    private final DirectoryService directoryService;

    @PostPersist
    public void createDirectoryForNewUser(User user) {
        directoryService.createDirectoryForNewUser(user);
        log.info("Created directory for user:  ({},{})", user.getUsername(), user.getId());
    }
}
