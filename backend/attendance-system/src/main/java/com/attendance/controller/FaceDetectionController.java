
package com.attendance.controller;

import com.attendance.service.FaceDetectionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
public class FaceDetectionController {

    private final FaceDetectionService faceDetectionService;

    public FaceDetectionController(
            FaceDetectionService faceDetectionService) {
        this.faceDetectionService = faceDetectionService;
    }

    @PostMapping(
            value = "/detect",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<?, ?>> detectFace(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<?, ?> result =
                faceDetectionService.detectFace(file);

        return ResponseEntity.ok(result);
    }
}
