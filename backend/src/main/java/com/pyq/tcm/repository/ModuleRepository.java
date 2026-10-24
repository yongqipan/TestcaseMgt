package com.pyq.tcm.repository;

import com.pyq.tcm.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByProjectIdOrderBySortOrderAscIdAsc(Long projectId);

    boolean existsByParentId(Long parentId);

    boolean existsByProjectId(Long projectId);
}
