
package com.attendance.repository;

import com.attendance.entity.FaceEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaceEmbeddingRepository
        extends JpaRepository<FaceEmbedding, Long> {

    Optional<FaceEmbedding> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
