package org.example.cloudstorage.controller;

import org.example.cloudstorage.dto.user.UsernameResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/user")
@RestController
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<UsernameResponseDto> me(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(new UsernameResponseDto(principal.getUsername()));
    }
}
