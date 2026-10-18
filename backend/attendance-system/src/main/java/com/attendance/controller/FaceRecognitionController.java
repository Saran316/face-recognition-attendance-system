
package com.attendance.controller;

import com.attendance.entity.User;
import com.attendance.repository.UserRepository;
import com.attendance.service.FaceRecognitionService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
public class FaceRecognitionController {

    private final UserRepository userRepository;
    private final FaceRecognitionService recognitionService;

    public FaceRecognitionController(
            UserRepository userRepository,
            FaceRecognitionService recognitionService) {
        this.userRepository = userRepository;
        this.recognitionService = recognitionService;
    }

    @PostMapping(
            value = "/recognize",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<?, ?>> recognizeFace(
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

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Authenticated user was not found."));

        Map<?, ?> result = recognitionService.recognize(
                user.getId(), file);

        return ResponseEntity.ok(result);
    }
}
