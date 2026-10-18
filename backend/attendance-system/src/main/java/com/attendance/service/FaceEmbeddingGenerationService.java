
package com.attendance.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FaceEmbeddingGenerationService {

    private final RestClient restClient =
            RestClient.builder()
                    .baseUrl("http://127.0.0.1:8001")
                    .build();

    public Map<?, ?> createEmbedding(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload an image.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            filename = "face.jpg";
        }

        final String uploadedFilename = filename;

        String contentType = file.getContentType();

        MediaType imageType =
                contentType != null
                        && contentType.startsWith("image/")
                        ? MediaType.parseMediaType(contentType)
                        : MediaType.IMAGE_JPEG;

        ByteArrayResource imageResource =
                new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return uploadedFilename;
                    }
                };

        MultipartBodyBuilder bodyBuilder =
                new MultipartBodyBuilder();

        bodyBuilder.part("file", imageResource)
                .filename(uploadedFilename)
                .contentType(imageType);

        Map<?, ?> result = restClient.post()
                .uri("/create-embedding")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(Map.class);

        if (result == null
                || !Boolean.TRUE.equals(result.get("success"))) {
            throw new IllegalStateException(
                    "Python service could not create the face embedding.");
        }

        return result;
    }
}
