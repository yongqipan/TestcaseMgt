package com.pyq.tcm.service;

import com.pyq.tcm.common.BizException;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.Project;
import com.pyq.tcm.repository.ModuleRepository;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import com.pyq.tcm.repository.TestPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestPlanRepository testPlanRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ModuleRepository moduleRepository,
                          TestCaseRepository testCaseRepository,
                          TestPlanRepository testPlanRepository) {
        this.projectRepository = projectRepository;
        this.moduleRepository = moduleRepository;
        this.testCaseRepository = testCaseRepository;
        this.testPlanRepository = testPlanRepository;
    }

    public PageResult<Project> page(String keyword, int page, int size) {
        Page<Project> result = projectRepository.search(
                keyword == null ? "" : keyword.trim(),
                PageRequest.of(Math.max(page, 1) - 1, clamp(size),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResult.of(result);
    }

    public List<Project> all() {
        return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Project get(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "项目不存在"));
    }

    @Transactional
    public Project create(Project project) {
        project.setId(null);
        project.setName(project.getName().trim());
        project.setCode(project.getCode().trim().toUpperCase());
        if (projectRepository.existsByCode(project.getCode())) {
            throw new BizException("项目编码已存在");
        }
        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, Project payload) {
        Project project = get(id);
        String code = payload.getCode().trim().toUpperCase();
        if (projectRepository.existsByCodeAndIdNot(code, id)) {
            throw new BizException("项目编码已存在");
        }
        project.setName(payload.getName().trim());
        project.setCode(code);
        project.setDescription(payload.getDescription());
        if (payload.getStatus() != null) {
            project.setStatus(payload.getStatus());
        }
        return projectRepository.save(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = get(id);
        if (moduleRepository.existsByProjectId(id)) {
            throw new BizException("项目下存在模块，请先删除模块");
        }
        if (testCaseRepository.countByProjectId(id) > 0) {
            throw new BizException("项目下存在测试用例，请先删除用例");
        }
        if (testPlanRepository.countByProjectId(id) > 0) {
            throw new BizException("项目下存在测试计划，请先删除计划");
        }
        projectRepository.delete(project);
    }

    private int clamp(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
