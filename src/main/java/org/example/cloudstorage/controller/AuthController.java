package org.example.cloudstorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.user.UserLoginRequest;
import org.example.cloudstorage.dto.user.UserRegisterRequest;
import org.example.cloudstorage.dto.user.UsernameResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.AuthService;
import org.example.cloudstorage.service.UserService;
import org.example.cloudstorage.util.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/sign-up")
    public ResponseEntity<UsernameResponseDto> signup(@RequestBody @Valid UserRegisterRequest request) {
        User user = userService.register(userMapper.toUser(request));
        authService.putUserInContextWithoutAuthentication(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsernameResponseDto(user.getUsername()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UsernameResponseDto> signin(@RequestBody @Valid UserLoginRequest request) {
        User user = authService.putUserInContextWithAuthentication(userMapper.toUser(request));
        return ResponseEntity.ok(new UsernameResponseDto(user.getUsername()));
    }
}
