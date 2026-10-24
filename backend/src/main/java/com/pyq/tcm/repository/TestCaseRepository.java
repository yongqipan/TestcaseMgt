package com.pyq.tcm.repository;

import com.pyq.tcm.entity.TestCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

    @Query("select t from TestCase t where t.projectId = :projectId " +
            "and (:moduleId is null or t.moduleId = :moduleId) " +
            "and (:kw = '' or lower(t.title) like lower(concat('%', :kw, '%')) " +
            "or lower(t.caseNo) like lower(concat('%', :kw, '%'))) " +
            "and (:priority = '' or t.priority = :priority) " +
            "and (:status = '' or t.status = :status)")
    Page<TestCase> search(@Param("projectId") Long projectId,
                          @Param("moduleId") Long moduleId,
                          @Param("kw") String kw,
                          @Param("priority") String priority,
                          @Param("status") String status,
                          Pageable pageable);

    List<TestCase> findByProjectIdOrderByIdAsc(Long projectId);

    long countByProjectId(Long projectId);

    long countByModuleId(Long moduleId);

    boolean existsByCaseNo(String caseNo);

    @Query("select t.priority, count(t) from TestCase t group by t.priority")
    List<Object[]> countGroupByPriority();

    @Query("select t.status, count(t) from TestCase t group by t.status")
    List<Object[]> countGroupByStatus();
}
