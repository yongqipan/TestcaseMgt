package com.pyq.tcm.repository;

import com.pyq.tcm.entity.TestPlanCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestPlanCaseRepository extends JpaRepository<TestPlanCase, Long> {

    List<TestPlanCase> findByPlanIdOrderByCaseIdAsc(Long planId);

    Optional<TestPlanCase> findByPlanIdAndCaseId(Long planId, Long caseId);

    boolean existsByPlanIdAndCaseId(Long planId, Long caseId);

    long countByPlanId(Long planId);

    void deleteByPlanId(Long planId);

    void deleteByPlanIdAndCaseId(Long planId, Long caseId);

    void deleteByCaseId(Long caseId);
}
