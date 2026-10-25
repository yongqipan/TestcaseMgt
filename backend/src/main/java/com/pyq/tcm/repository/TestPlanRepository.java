package com.pyq.tcm.repository;

import com.pyq.tcm.entity.TestPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestPlanRepository extends JpaRepository<TestPlan, Long> {

    @Query("select p from TestPlan p where p.projectId = :projectId " +
            "and (:kw = '' or lower(p.name) like lower(concat('%', :kw, '%')))")
    Page<TestPlan> search(@Param("projectId") Long projectId,
                          @Param("kw") String kw,
                          Pageable pageable);

    long countByProjectId(Long projectId);

    List<TestPlan> findTop5ByOrderByCreatedAtDesc();

    @Query("select p from TestPlan p where p.casesInitialized is null or p.casesInitialized = false")
    List<TestPlan> findUninitializedPlans();
}
