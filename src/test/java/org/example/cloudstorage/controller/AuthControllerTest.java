package org.example.cloudstorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.cloudstorage.dto.user.UserLoginRequest;
import org.example.cloudstorage.dto.user.UserRegisterRequest;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.exception.UserWithThisNameAlreadyExistsException;
import org.example.cloudstorage.service.AuthService;
import org.example.cloudstorage.service.UserService;
import org.example.cloudstorage.util.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.example.cloudstorage.controller.AuthControllerTest.Stub.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    UserMapper userMapper;

    @Autowired
    MockMvc mvc;

    static class Stub {
        static final String USERNAME = "username";
        static final String PASSWORD = "password";
        static final User USER;
        static final UserLoginRequest LOGIN_REQUEST = new UserLoginRequest(USERNAME, PASSWORD);
        static final UserRegisterRequest REGISTER_REQUEST = new UserRegisterRequest(USERNAME, PASSWORD);

        static {
            USER = new User();
            USER.setUsername(USERNAME);
            USER.setPassword(PASSWORD);
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
    @DisplayName("signup with correct data -> services called and username returned")
    void signup_correctData_servicesCalled_UsernameReturned() throws Exception {
        doReturn(USER).when(userMapper).toUser(REGISTER_REQUEST);
        doReturn(USER).when(userService).register(USER);
        doNothing().when(authService).putUserInContextWithoutAuthentication(USER);

        mvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(REGISTER_REQUEST))
        ).andExpectAll(
                status().is(HttpStatus.CREATED.value()),
                r -> jsonPath("$.username", equalTo(USERNAME))
        );

        verify(userService, times(1)).register(USER);
        verify(authService, times(1)).putUserInContextWithoutAuthentication(USER);
    }

    @Test
    @DisplayName("try sign up when username already exists -> throws error")
    void signup_usernameAlreadyExists_throws() throws Exception {
        doReturn(USER).when(userMapper).toUser(REGISTER_REQUEST);

        doThrow(new UserWithThisNameAlreadyExistsException("already exists")).when(userService).register(USER);

        mvc.perform(
                post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(REGISTER_REQUEST))
        ).andExpectAll(
                status().is(HttpStatus.CONFLICT.value()),
                jsonPath("$.message", containsString("already exists"))
        );

        verifyNoInteractions(authService);
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
                status().is(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.message", containsString("username"))
        );

        verifyNoInteractions(userService);
        verifyNoInteractions(authService);
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
                status().is(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.message", containsString("password"))
        );

        verifyNoInteractions(userService);
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("signin with correct data -> services called and username returned")
    void signin_correctData_servicesCalled_UsernameReturned() throws Exception {
        doReturn(USER).when(userMapper).toUser(LOGIN_REQUEST);
        doReturn(USER).when(authService).putUserInContextWithAuthentication(USER);

        mvc.perform(
                post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(REGISTER_REQUEST))
        ).andExpectAll(
                status().is(HttpStatus.OK.value()),
                jsonPath("$.username", equalTo(USERNAME))
        );

        verify(authService, times(1)).putUserInContextWithAuthentication(USER);
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
                status().is(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.message", containsString("username"))

        );

        verifyNoInteractions(userService);
        verifyNoInteractions(authService);
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
                status().is(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.message", containsString("password"))
        );

        verifyNoInteractions(userService);
        verifyNoInteractions(authService);
    }
}