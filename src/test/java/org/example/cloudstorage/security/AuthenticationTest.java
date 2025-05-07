package org.example.cloudstorage.security;

import org.example.cloudstorage.config.security.AuthConfig;
import org.example.cloudstorage.config.security.SecurityConfig;
import org.example.cloudstorage.controller.UserController;
import org.example.cloudstorage.mapper.UserMapper;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, AuthConfig.class})
@AutoConfigureMockMvc
public class AuthenticationTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    UserMapper userMapper;


    @Test
    @DisplayName("Unauthenticated user cannot access sign-out endpoint and get 401")
    void authentication_unauthenticated_notAllowedToSignOutEndpoint() throws Exception {
        mvc.perform(
                post("/auth/sign-out")
        ).andExpectAll(
                status().isUnauthorized()
        );
    }

    @Test
    @WithMockUser(username = "dummy")
    @DisplayName("Authenticated user can access sign-out endpoint and get 204")
    void authentication_authenticated_AllowedToSignOutEndpoint() throws Exception {
        mvc.perform(
                post("/auth/sign-out")
        ).andExpectAll(
                status().isNoContent()
        );
    }
}
