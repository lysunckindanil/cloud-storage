package org.example.cloudstorage.listener;

import jakarta.persistence.PostPersist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.DirectoryService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateInitialUserFolderEntityListener {
    private final DirectoryService directoryService;
    private static final int RETRY_ATTEMPTS = 5;
    private static final int RETRY_TIMEOUT = 1000;

    @PostPersist
    public void createDirectoryForNewUser(User user) throws InterruptedException {
        for (int i = 0; i < RETRY_ATTEMPTS; i++) {
            try {
                directoryService.createDirectoryForNewUser(user);
                log.info("Created directory for user:  ({},{})", user.getUsername(), user.getId());
                return;
            } catch (Exception e) {
                log.error("Failed to create directory for user:  ({},{})", user.getUsername(), user.getId());
                Thread.sleep(RETRY_TIMEOUT);
            }
        }
    }
}