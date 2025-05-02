package org.example.cloudstorage.controller;

import org.example.cloudstorage.dto.user.UsernameResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RequestMapping("/user")
@RestController
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<UsernameResponseDto> me(Principal principal) {
        return ResponseEntity.ok(new UsernameResponseDto(principal.getName()));
    }
}
