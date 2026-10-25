package com.pyq.tcm.service;

import com.pyq.tcm.common.BizException;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.CaseStep;
import com.pyq.tcm.entity.Project;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.repository.ModuleRepository;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import com.pyq.tcm.repository.TestPlanCaseRepository;
import com.pyq.tcm.repository.TestRunRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestCaseService {

    private static final List<String> PRIORITIES = List.of("P0", "P1", "P2", "P3");
    private static final List<String> TYPES = List.of("FUNCTIONAL", "API", "UI", "PERFORMANCE");
    private static final List<String> STATUSES = List.of("DRAFT", "ACTIVE", "DEPRECATED");

    private final TestCaseRepository testCaseRepository;
    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final TestRunRepository testRunRepository;
    private final TestPlanCaseRepository testPlanCaseRepository;

    public TestCaseService(TestCaseRepository testCaseRepository,
                           ProjectRepository projectRepository,
                           ModuleRepository moduleRepository,
                           TestRunRepository testRunRepository,
                           TestPlanCaseRepository testPlanCaseRepository) {
        this.testCaseRepository = testCaseRepository;
        this.projectRepository = projectRepository;
        this.moduleRepository = moduleRepository;
        this.testRunRepository = testRunRepository;
        this.testPlanCaseRepository = testPlanCaseRepository;
    }

    public PageResult<TestCase> page(Long projectId, Long moduleId, String keyword,
                                     String priority, String status, int page, int size) {
        if (projectId == null) {
            throw new BizException("请先选择项目");
        }
        Page<TestCase> result = testCaseRepository.search(projectId, moduleId,
                keyword == null ? "" : keyword.trim(),
                priority == null ? "" : priority,
                status == null ? "" : status,
                PageRequest.of(Math.max(page, 1) - 1, clamp(size),
                        Sort.by(Sort.Direction.DESC, "updatedAt")));
        return PageResult.of(result);
    }

    public TestCase get(Long id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "测试用例不存在"));
    }

    @Transactional
    public TestCase create(TestCase payload, String creator) {
        validate(payload);
        Project project = projectRepository.findById(payload.getProjectId())
                .orElseThrow(() -> new BizException("项目不存在"));
        payload.setId(null);
        payload.setCaseNo(nextCaseNo(project));
        payload.setCreator(creator);
        normalize(payload);
        return testCaseRepository.save(payload);
    }

    @Transactional
    public TestCase update(Long id, TestCase payload) {
        validate(payload);
        TestCase testCase = get(id);
        testCase.setTitle(payload.getTitle().trim());
        testCase.setModuleId(payload.getModuleId());
        testCase.setPriority(payload.getPriority());
        testCase.setType(payload.getType());
        testCase.setStatus(payload.getStatus());
        testCase.setPrecondition(payload.getPrecondition());
        testCase.setTags(payload.getTags());
        testCase.setSteps(payload.getSteps() == null ? new ArrayList<>() : payload.getSteps());
        normalize(testCase);
        return testCaseRepository.save(testCase);
    }

    @Transactional
    public void delete(Long id) {
        TestCase testCase = get(id);
        testRunRepository.deleteByCaseId(id);
        testPlanCaseRepository.deleteByCaseId(id);
        testCaseRepository.delete(testCase);
    }

    public List<TestCase> listByProject(Long projectId) {
        return testCaseRepository.findByProjectIdOrderByIdAsc(projectId);
    }

    private void validate(TestCase payload) {
        if (payload.getProjectId() == null) {
            throw new BizException("请选择所属项目");
        }
        if (payload.getTitle() == null || payload.getTitle().isBlank()) {
            throw new BizException("用例标题不能为空");
        }
        if (payload.getModuleId() != null && !moduleRepository.existsById(payload.getModuleId())) {
            throw new BizException("所选模块不存在");
        }
    }

    private void normalize(TestCase testCase) {
        if (testCase.getPriority() == null || !PRIORITIES.contains(testCase.getPriority())) {
            testCase.setPriority("P1");
        }
        if (testCase.getType() == null || !TYPES.contains(testCase.getType())) {
            testCase.setType("FUNCTIONAL");
        }
        if (testCase.getStatus() == null || !STATUSES.contains(testCase.getStatus())) {
            testCase.setStatus("ACTIVE");
        }
        if (testCase.getSteps() != null) {
            for (CaseStep step : testCase.getSteps()) {
                if (step.getAction() != null) {
                    step.setAction(step.getAction().trim());
                }
                if (step.getExpected() != null) {
                    step.setExpected(step.getExpected().trim());
                }
            }
        }
    }

    private String nextCaseNo(Project project) {
        long sequence = testCaseRepository.countByProjectId(project.getId()) + 1;
        String code = project.getCode();
        String caseNo = String.format("TC-%s-%03d", code, sequence);
        while (testCaseRepository.existsByCaseNo(caseNo)) {
            sequence++;
            caseNo = String.format("TC-%s-%03d", code, sequence);
        }
        return caseNo;
    }

    private int clamp(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
