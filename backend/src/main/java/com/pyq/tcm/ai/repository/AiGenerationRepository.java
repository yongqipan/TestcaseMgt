package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiGeneration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiGenerationRepository extends JpaRepository<AiGeneration, Long> {

    @Query("select g from AiGeneration g where g.projectId = :projectId " +
            "and (:scene = '' or g.scene = :scene) " +
            "and (:status = '' or g.status = :status)")
    Page<AiGeneration> search(@Param("projectId") Long projectId,
                              @Param("scene") String scene,
                              @Param("status") String status,
                              Pageable pageable);

    long countByProjectIdAndStatus(Long projectId, String status);
}
