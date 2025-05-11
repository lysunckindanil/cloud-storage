package org.example.cloudstorage.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cloudstorage.config.MinioMockConfig;
import org.example.cloudstorage.config.PostgresContainerConfig;
import org.example.cloudstorage.config.RedisContainerConfig;
import org.example.cloudstorage.dto.user.UserLoginRequest;
import org.example.cloudstorage.dto.user.UserRegisterRequest;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.example.cloudstorage.security.AuthenticationValidationTest.Stub.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@SpringBootTest
@Import({PostgresContainerConfig.class, RedisContainerConfig.class, MinioMockConfig.class})
@AutoConfigureMockMvc
@Transactional
class AuthenticationValidationTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserService userService;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DirectoryService directoryService;

    static class Stub {
        static final String USERNAME = "username";
        static final String PASSWORD = "password";
        static final UserLoginRequest LOGIN_REQUEST = new UserLoginRequest(USERNAME, PASSWORD);
        static final UserRegisterRequest REGISTER_REQUEST = new UserRegisterRequest(USERNAME, PASSWORD);

        static User USER() {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = new User();
            user.setUsername(USERNAME);
            user.setPassword(encoder.encode(PASSWORD));
            return user;
        }
    }

    private static Stream<Arguments> invalidCredentialsProvider() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("      "),
                Arguments.of("12"),
                Arguments.of("1".repeat(21))
        );
    }

    @Test
    @DisplayName("signup with correct data -> user registered and username returned")
    void signup_correctData_servicesCalled_UsernameReturned() throws Exception {
        mvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(REGISTER_REQUEST))
        ).andExpectAll(
                status().isCreated(),
                r -> jsonPath("$.username", equalTo(USERNAME))
        );
        Assertions.assertTrue(userService.findByUsername(USERNAME).isPresent());
    }

    @Test
    @DisplayName("try sign up when username already exists -> throws error")
    void signup_usernameAlreadyExists_throws() throws Exception {
        userService.register(USER());

        mvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(REGISTER_REQUEST))
        ).andExpectAll(
                status().isConflict(),
                jsonPath("$.message", containsString("already exists"))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialsProvider")
    @DisplayName("try sign up with invalid username -> throws username validation error")
    void signup_invalidUsername_throws(String username) throws Exception {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(null, username);

        mvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRegisterRequest))
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.message", containsString("username"))
        );
    }


    @ParameterizedTest
    @MethodSource("invalidCredentialsProvider")
    @DisplayName("try sign up with invalid password -> throws password validation error")
    void signup_invalidPassword_throws(String password) throws Exception {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(USERNAME, password);

        mvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRegisterRequest))
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.message", containsString("password"))
        );
    }

    @Test
    @DisplayName("signin with correct data -> username returned")
    void signin_correctData_servicesCalled_UsernameReturned() throws Exception {
        userService.register(USER());

        mvc.perform(
                post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(LOGIN_REQUEST))
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.username", equalTo(USERNAME))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialsProvider")
    @DisplayName("try sign in with invalid username -> throws username validation error")
    void signin_invalidUsername_throws(String username) throws Exception {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(null, username);

        mvc.perform(
                post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRegisterRequest))
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.message", containsString("username"))

        );
    }


    @ParameterizedTest
    @MethodSource("invalidCredentialsProvider")
    @DisplayName("try sign in with invalid password -> throws password validation error")
    void signin_invalidPassword_throws(String password) throws Exception {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(USERNAME, password);

        mvc.perform(
                post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userRegisterRequest))
        ).andExpectAll(
                status().isBadRequest(),
                jsonPath("$.message", containsString("password"))
        );
    }
}