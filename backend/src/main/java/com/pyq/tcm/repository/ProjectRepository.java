package com.pyq.tcm.repository;

import com.pyq.tcm.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("select p from Project p where (:kw = '' or lower(p.name) like lower(concat('%', :kw, '%')) " +
            "or lower(p.code) like lower(concat('%', :kw, '%')))")
    Page<Project> search(@Param("kw") String kw, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
