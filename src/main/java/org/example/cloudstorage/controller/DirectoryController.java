package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.DirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/directory")
@RequiredArgsConstructor
public class DirectoryController {
    private final DirectoryService directoryService;

    @GetMapping
    public ResponseEntity<List<ResourceResponseDto>> get(@RequestParam("path") String path, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(directoryService.get(path, user));
    }

    @PostMapping
    public ResponseEntity<ResourceResponseDto> create(@RequestParam("path") String path, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(directoryService.create(path, user));
    }
}
