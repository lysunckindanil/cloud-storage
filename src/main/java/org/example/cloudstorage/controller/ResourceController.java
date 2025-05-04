package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.ResourceService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<ResourceResponseDto> get(@RequestParam("path") String path, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resourceService.get(path, user));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("path") String path, @AuthenticationPrincipal User user) {
        resourceService.delete(path, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("path") String path, @AuthenticationPrincipal User user) {
        InputStreamResource object = resourceService.download(path, user);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(path, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(object);
    }

    @GetMapping("/move")
    public ResponseEntity<ResourceResponseDto> move(@RequestParam("from") String from, @RequestParam("to") String to,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resourceService.move(from, to, user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> search(@RequestParam("query") String query, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resourceService.search(query, user));
    }

    @PostMapping
    public ResponseEntity<List<ResourceResponseDto>> upload(@RequestParam("path") String path,
                                                            @RequestParam("object") List<MultipartFile> files,
                                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.upload(path, files, user));
    }
}