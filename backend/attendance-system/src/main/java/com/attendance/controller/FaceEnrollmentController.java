
package com.attendance.controller;

import com.attendance.entity.User;
import com.attendance.repository.UserRepository;
import com.attendance.service.FaceEmbeddingGenerationService;
import com.attendance.service.FaceEmbeddingService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
public class FaceEnrollmentController {

    private final UserRepository userRepository;
    private final FaceEmbeddingGenerationService generationService;
    private final FaceEmbeddingService embeddingService;

    public FaceEnrollmentController(
            UserRepository userRepository,
            FaceEmbeddingGenerationService generationService,
            FaceEmbeddingService embeddingService) {
        this.userRepository = userRepository;
        this.generationService = generationService;
        this.embeddingService = embeddingService;
    }

    @PostMapping(
            value = "/enroll",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> enrollFace(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Please upload a face image."));
        }

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Please log in first."));
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user was not found."));

        Map<?, ?> result =
                generationService.createEmbedding(file);

        String modelName = String.valueOf(result.get("model"));
        Object embeddingObject = result.get("embedding");

        if (!(embeddingObject instanceof java.util.List<?>)) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message",
                            "Invalid embedding returned by face service."));
        }

        String embeddingJson =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(embeddingObject);

        embeddingService.enroll(user, modelName, embeddingJson);

        return ResponseEntity.ok(
                Map.of("message",
                        "Face enrolled successfully."));
    }
}
