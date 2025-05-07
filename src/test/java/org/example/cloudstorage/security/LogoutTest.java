package org.example.cloudstorage.security;

import org.example.cloudstorage.config.MinioMockConfig;
import org.example.cloudstorage.config.PostgresContainerConfig;
import org.example.cloudstorage.config.RedisContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Import({PostgresContainerConfig.class, RedisContainerConfig.class, MinioMockConfig.class})
@AutoConfigureMockMvc
public class LogoutTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Unauthenticated user cannot access sign-out endpoint and gets 401")
    void authentication_unauthenticated_notAllowedToSignOutEndpoint() throws Exception {
        mvc.perform(
                post("/auth/sign-out")
                        .servletPath("/auth/sign-out")
        ).andExpectAll(
                status().isUnauthorized(),
                jsonPath("$.message", containsString("You are not logged in to log out"))
        );
    }

    @Test
    @WithMockUser(username = "dummy")
    @DisplayName("Authenticated user can access sign-out endpoint and gets 204")
    void authentication_authenticated_AllowedToSignOutEndpoint() throws Exception {
        mvc.perform(
                post("/auth/sign-out")
                        .servletPath("/auth/sign-out")
        ).andExpectAll(
                status().isNoContent()
        );
    }
}
