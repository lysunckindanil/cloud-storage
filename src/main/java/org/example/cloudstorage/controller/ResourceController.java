package org.example.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.ResourceResponseDto;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.service.ResourceService;
import org.example.cloudstorage.util.validation.Path;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Validated
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<ResourceResponseDto> get(@Path @RequestParam("path") String path,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resourceService.get(path, user));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@Path @RequestParam("path") String path,
                                       @AuthenticationPrincipal User user) {
        resourceService.delete(path, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@Path @RequestParam("path") String path,
                                             @AuthenticationPrincipal User user) {
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
    public ResponseEntity<ResourceResponseDto> move(@Path @RequestParam("from") String from,
                                                    @Path @RequestParam("to") String to,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resourceService.move(from, to, user));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResourceResponseDto>> search(@Path @RequestParam("query") String query,
                                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resourceService.search(query, user));
    }

    @PostMapping
    public ResponseEntity<List<ResourceResponseDto>> upload(@Path @RequestParam("path") String path,
                                                            @RequestParam("object") List<MultipartFile> files,
                                                            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.upload(path, files, user));
    }
}