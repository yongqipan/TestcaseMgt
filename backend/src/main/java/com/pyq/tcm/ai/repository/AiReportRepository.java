package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiReportRepository extends JpaRepository<AiReport, Long> {

    List<AiReport> findByPlanIdOrderByCreatedAtDesc(Long planId);

    Optional<AiReport> findFirstByPlanIdOrderByCreatedAtDesc(Long planId);

    void deleteByPlanId(Long planId);
}
