package org.example.cloudstorage.minio.impl;

import org.example.cloudstorage.config.MinioMockConfig;
import org.example.cloudstorage.config.PostgresContainerConfig;
import org.example.cloudstorage.config.RedisContainerConfig;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.model.Role;
import org.example.cloudstorage.repo.UserRepository;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.service.UserService;
import org.example.cloudstorage.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@DataJpaTest
@Import({PostgresContainerConfig.class, RedisContainerConfig.class, MinioMockConfig.class})
public class CreateFolderAfterRegistrationTest {

    @MockitoBean
    DirectoryService directoryService;

    private final UserService userService;

    @Autowired
    public CreateFolderAfterRegistrationTest(UserRepository userRepository) {
        this.userService = new UserServiceImpl(userRepository);
    }


    @Test
    public void testCreateFolderAfterRegistration() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("test");
        user.setRole(Role.ROLE_USER);
        userService.register(user);

        verify(directoryService, times(1)).createDirectoryForNewUser(user);
    }
}
