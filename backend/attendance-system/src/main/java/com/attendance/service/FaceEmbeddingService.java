
package com.attendance.service;

import com.attendance.entity.FaceEmbedding;
import com.attendance.entity.User;
import com.attendance.repository.FaceEmbeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FaceEmbeddingService {

    private final FaceEmbeddingRepository repository;

    public FaceEmbeddingService(FaceEmbeddingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FaceEmbedding enroll(
            User user,
            String modelName,
            String embedding
    ) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "A registered user is required."
            );
        }

        if (modelName == null || !modelName.equals("Facenet512")) {
            throw new IllegalArgumentException(
                    "Unsupported face model."
            );
        }

        if (embedding == null || embedding.isBlank()) {
            throw new IllegalArgumentException(
                    "Face embedding cannot be empty."
            );
        }

        FaceEmbedding faceEmbedding = repository
                .findByUserId(user.getId())
                .orElseGet(FaceEmbedding::new);

        faceEmbedding.setUser(user);
        faceEmbedding.setModelName(modelName);
        faceEmbedding.setEmbedding(embedding);

        return repository.save(faceEmbedding);
    }
    @Transactional(readOnly = true)
public FaceEmbedding getByUserId(Long userId) {
    return repository.findByUserId(userId)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "No face is enrolled for this user."));
}
}
