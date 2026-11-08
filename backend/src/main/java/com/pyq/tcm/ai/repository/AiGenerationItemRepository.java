package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiGenerationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiGenerationItemRepository extends JpaRepository<AiGenerationItem, Long> {

    List<AiGenerationItem> findByGenerationIdOrderByIdAsc(Long generationId);

    List<AiGenerationItem> findByGenerationIdAndSelectedTrueOrderByIdAsc(Long generationId);

    long countByGenerationId(Long generationId);

    void deleteByGenerationId(Long generationId);
}
