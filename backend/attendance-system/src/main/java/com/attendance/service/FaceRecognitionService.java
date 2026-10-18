
package com.attendance.service;

import com.attendance.entity.FaceEmbedding;
import com.attendance.repository.FaceEmbeddingRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FaceRecognitionService {

    private final FaceEmbeddingRepository embeddingRepository;

    private final RestClient restClient =
            RestClient.builder()
                    .baseUrl("http://127.0.0.1:8001")
                    .build();

    public FaceRecognitionService(
            FaceEmbeddingRepository embeddingRepository) {
        this.embeddingRepository = embeddingRepository;
    }

    public Map<?, ?> recognize(Long userId, MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a face image.");
        }

        FaceEmbedding enrolled = embeddingRepository
                .findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No face is enrolled for this user."));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "face.jpg";
        }

        final String uploadedFilename = filename;

        String contentType = file.getContentType();
        MediaType imageType =
                contentType != null && contentType.startsWith("image/")
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

        bodyBuilder.part(
                "enrolled_embedding",
                enrolled.getEmbedding()
        );

        bodyBuilder.part("new_image", imageResource)
                .filename(uploadedFilename)
                .contentType(imageType);

        try {
            Map<?, ?> result = restClient.post()
                    .uri("/compare-embeddings")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(bodyBuilder.build())
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new IllegalStateException(
                                        "Face service returned HTTP "
                                                + response.getStatusCode());
                            })
                    .body(Map.class);

            if (result == null) {
                throw new IllegalStateException(
                        "Face service returned an empty response.");
            }

            return result;

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to communicate with the Python face service.",
                    ex);
        }
    }
}
