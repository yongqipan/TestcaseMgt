package com.pyq.tcm.repository;

import com.pyq.tcm.entity.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    List<TestRun> findByPlanId(Long planId);

    Optional<TestRun> findByPlanIdAndCaseId(Long planId, Long caseId);

    void deleteByCaseId(Long caseId);

    void deleteByPlanId(Long planId);

    long countByStatus(String status);

    List<TestRun> findTop8ByOrderByExecutedAtDesc();

    long countByCaseId(Long caseId);
}
