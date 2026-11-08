package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiReviewRepository extends JpaRepository<AiReview, Long> {

    List<AiReview> findByCaseIdOrderByCreatedAtDesc(Long caseId);

    Optional<AiReview> findFirstByCaseIdOrderByCreatedAtDesc(Long caseId);

    void deleteByCaseId(Long caseId);
}
