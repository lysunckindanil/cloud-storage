package org.example.cloudstorage.controller;

import org.example.cloudstorage.config.security.AuthConfig;
import org.example.cloudstorage.config.security.SecurityConfig;
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

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, AuthConfig.class})
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    UserMapper userMapper;


    @Test
    @WithMockUser(username = "dummy")
    @DisplayName("Authenticated user -> returns username")
    void me_authenticated_returnsUsername() throws Exception {
        mvc.perform(
                get("/user/me")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.username", equalTo("dummy"))
        );
    }

    @Test
    @DisplayName("Unauthenticated user -> throws")
    void me_unauthenticated_throws() throws Exception {
        mvc.perform(
                get("/user/me")

        ).andExpect(
                status().isUnauthorized()
        );
    }

}