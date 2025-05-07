package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.DirectoryService;
import org.example.cloudstorage.util.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
public class DirectoryController {
    private final DirectoryService directoryService;

    @GetMapping
    public ResponseEntity<List<ResourceResponseDto>> get(@Path @RequestParam("path") String path,
                                                         @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(directoryService.get(path, user));
    }

    @PostMapping
    public ResponseEntity<ResourceResponseDto> create(@Path @RequestParam("path") String path,
                                                      @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directoryService.create(path, user));
    }
}