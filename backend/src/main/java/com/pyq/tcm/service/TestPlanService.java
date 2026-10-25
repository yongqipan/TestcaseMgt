package com.pyq.tcm.service;

import com.pyq.tcm.common.BizException;
import com.pyq.tcm.common.PageResult;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.entity.TestPlan;
import com.pyq.tcm.entity.TestPlanCase;
import com.pyq.tcm.entity.TestRun;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import com.pyq.tcm.repository.TestPlanCaseRepository;
import com.pyq.tcm.repository.TestPlanRepository;
import com.pyq.tcm.repository.TestRunRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TestPlanService {

    private static final Set<String> RUN_STATUSES = Set.of("PASS", "FAIL", "BLOCKED", "SKIPPED");
    private static final int MAX_CASES_PER_ADD = 500;

    private final TestPlanRepository testPlanRepository;
    private final TestPlanCaseRepository testPlanCaseRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestRunRepository testRunRepository;
    private final ProjectRepository projectRepository;

    public TestPlanService(TestPlanRepository testPlanRepository,
                           TestPlanCaseRepository testPlanCaseRepository,
                           TestCaseRepository testCaseRepository,
                           TestRunRepository testRunRepository,
                           ProjectRepository projectRepository) {
        this.testPlanRepository = testPlanRepository;
        this.testPlanCaseRepository = testPlanCaseRepository;
        this.testCaseRepository = testCaseRepository;
        this.testRunRepository = testRunRepository;
        this.projectRepository = projectRepository;
    }

    public PageResult<TestPlan> page(Long projectId, String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, clamp(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TestPlan> result;
        if (projectId == null) {
            result = testPlanRepository.findAll(pageable);
        } else {
            result = testPlanRepository.search(projectId, keyword == null ? "" : keyword.trim(), pageable);
        }
        return PageResult.of(result);
    }

    public TestPlan get(Long id) {
        return testPlanRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "测试计划不存在"));
    }

    @Transactional
    public TestPlan create(TestPlan payload, String creator) {
        if (payload.getProjectId() == null || !projectRepository.existsById(payload.getProjectId())) {
            throw new BizException("项目不存在");
        }
        if (payload.getName() == null || payload.getName().isBlank()) {
            throw new BizException("计划名称不能为空");
        }
        payload.setId(null);
        payload.setName(payload.getName().trim());
        payload.setCreatedBy(creator);
        payload.setCasesInitialized(true);
        return testPlanRepository.save(payload);
    }

    @Transactional
    public TestPlan update(Long id, TestPlan payload) {
        TestPlan plan = get(id);
        if (payload.getName() == null || payload.getName().isBlank()) {
            throw new BizException("计划名称不能为空");
        }
        plan.setName(payload.getName().trim());
        plan.setDescription(payload.getDescription());
        plan.setStatus(payload.getStatus());
        plan.setStartDate(payload.getStartDate());
        plan.setEndDate(payload.getEndDate());
        return testPlanRepository.save(plan);
    }

    @Transactional
    public void delete(Long id) {
        TestPlan plan = get(id);
        testRunRepository.deleteByPlanId(id);
        testPlanCaseRepository.deleteByPlanId(id);
        testPlanRepository.delete(plan);
    }

    public Map<String, Object> detail(Long planId) {
        TestPlan plan = get(planId);
        List<TestCase> cases = selectedCases(planId);
        Map<Long, TestRun> runs = testRunRepository.findByPlanId(planId).stream()
                .collect(Collectors.toMap(TestRun::getCaseId, Function.identity(), (a, b) -> b));

        List<Map<String, Object>> items = new ArrayList<>();
        for (TestCase testCase : cases) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", testCase.getId());
            item.put("caseNo", testCase.getCaseNo());
            item.put("title", testCase.getTitle());
            item.put("priority", testCase.getPriority());
            item.put("type", testCase.getType());
            item.put("caseStatus", testCase.getStatus());
            item.put("precondition", testCase.getPrecondition());
            item.put("steps", testCase.getSteps());
            TestRun run = runs.get(testCase.getId());
            item.put("runStatus", run == null ? "UNTESTED" : run.getStatus());
            item.put("executor", run == null ? null : run.getExecutor());
            item.put("remark", run == null ? null : run.getRemark());
            item.put("executedAt", run == null ? null : run.getExecutedAt());
            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("plan", plan);
        result.put("cases", items);
        result.put("stats", calculateStats(items));
        return result;
    }

    public PageResult<TestCase> availableCases(Long planId, Long moduleId, String keyword,
                                               String priority, String status, int page, int size) {
        TestPlan plan = get(planId);
        Page<TestCase> result = testCaseRepository.searchAvailable(
                plan.getProjectId(),
                planId,
                moduleId,
                keyword == null ? "" : keyword.trim(),
                priority == null ? "" : priority,
                status == null ? "" : status,
                PageRequest.of(Math.max(page, 1) - 1, clamp(size),
                        Sort.by(Sort.Direction.DESC, "updatedAt")));
        return PageResult.of(result);
    }

    @Transactional
    public int addCases(Long planId, List<Long> caseIds) {
        TestPlan plan = get(planId);
        List<Long> ids = caseIds == null ? List.of() : caseIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            throw new BizException("请选择要添加的用例");
        }
        if (ids.size() > MAX_CASES_PER_ADD) {
            throw new BizException("单次最多添加 " + MAX_CASES_PER_ADD + " 条用例");
        }

        List<TestCase> cases = testCaseRepository.findAllById(ids);
        if (cases.size() != ids.size()) {
            throw new BizException("存在已删除的用例，请刷新后重试");
        }
        for (TestCase testCase : cases) {
            if (!plan.getProjectId().equals(testCase.getProjectId())) {
                throw new BizException("用例「" + testCase.getCaseNo() + "」不属于当前计划的项目");
            }
        }

        Set<Long> existing = testPlanCaseRepository.findByPlanIdOrderByCaseIdAsc(planId).stream()
                .map(TestPlanCase::getCaseId)
                .collect(Collectors.toSet());
        List<TestPlanCase> links = new ArrayList<>();
        for (Long caseId : ids) {
            if (existing.contains(caseId)) {
                continue;
            }
            TestPlanCase link = new TestPlanCase();
            link.setPlanId(planId);
            link.setCaseId(caseId);
            links.add(link);
        }
        if (!links.isEmpty()) {
            testPlanCaseRepository.saveAll(links);
        }
        return links.size();
    }

    @Transactional
    public void removeCase(Long planId, Long caseId) {
        get(planId);
        TestPlanCase link = testPlanCaseRepository.findByPlanIdAndCaseId(planId, caseId)
                .orElseThrow(() -> new BizException(404, "该用例不在当前计划中"));
        testRunRepository.deleteByPlanIdAndCaseId(planId, caseId);
        testPlanCaseRepository.delete(link);
    }

    @Transactional
    public TestRun submitRun(Long planId, Long caseId, String status, String remark, String executor) {
        TestPlan plan = get(planId);
        if (caseId == null) {
            throw new BizException("请选择测试用例");
        }
        if (!testPlanCaseRepository.existsByPlanIdAndCaseId(planId, caseId)) {
            throw new BizException("该用例未加入当前计划，请先添加后再执行");
        }
        TestCase testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> new BizException(404, "测试用例不存在"));
        if (!testCase.getProjectId().equals(plan.getProjectId())) {
            throw new BizException("该用例不属于当前计划的项目");
        }
        if (status == null || !RUN_STATUSES.contains(status)) {
            throw new BizException("执行结果不合法");
        }
        TestRun run = testRunRepository.findByPlanIdAndCaseId(planId, caseId).orElseGet(TestRun::new);
        run.setPlanId(planId);
        run.setCaseId(caseId);
        run.setStatus(status);
        run.setRemark(remark);
        run.setExecutor(executor == null || executor.isBlank() ? "tester" : executor);
        run.setExecutedAt(LocalDateTime.now());
        TestRun saved = testRunRepository.save(run);

        if ("PLANNED".equals(plan.getStatus())) {
            plan.setStatus("IN_PROGRESS");
            testPlanRepository.save(plan);
        }
        return saved;
    }

    public Map<String, Object> stats(Long planId) {
        get(planId);
        List<TestCase> cases = selectedCases(planId);
        Map<Long, TestRun> runs = testRunRepository.findByPlanId(planId).stream()
                .collect(Collectors.toMap(TestRun::getCaseId, Function.identity(), (a, b) -> b));
        long pass = 0;
        long fail = 0;
        long blocked = 0;
        long skipped = 0;
        for (TestCase testCase : cases) {
            TestRun run = runs.get(testCase.getId());
            if (run == null) {
                continue;
            }
            switch (run.getStatus()) {
                case "PASS" -> pass++;
                case "FAIL" -> fail++;
                case "BLOCKED" -> blocked++;
                case "SKIPPED" -> skipped++;
                default -> {
                }
            }
        }
        long executed = pass + fail + blocked + skipped;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", cases.size());
        result.put("executed", executed);
        result.put("untested", cases.size() - executed);
        result.put("pass", pass);
        result.put("fail", fail);
        result.put("blocked", blocked);
        result.put("skipped", skipped);
        result.put("passRate", executed == 0 ? 0.0 : round(pass * 100.0 / executed));
        return result;
    }

    @Transactional
    public void initializeLegacyPlans() {
        for (TestPlan plan : testPlanRepository.findUninitializedPlans()) {
            if (testPlanCaseRepository.findByPlanIdOrderByCaseIdAsc(plan.getId()).isEmpty()) {
                List<TestPlanCase> links = testCaseRepository.findByProjectIdOrderByIdAsc(plan.getProjectId()).stream()
                        .map(testCase -> {
                            TestPlanCase link = new TestPlanCase();
                            link.setPlanId(plan.getId());
                            link.setCaseId(testCase.getId());
                            return link;
                        })
                        .toList();
                if (!links.isEmpty()) {
                    testPlanCaseRepository.saveAll(links);
                }
            }
            plan.setCasesInitialized(true);
            testPlanRepository.save(plan);
        }
    }

    private List<TestCase> selectedCases(Long planId) {
        List<Long> caseIds = testPlanCaseRepository.findByPlanIdOrderByCaseIdAsc(planId).stream()
                .map(TestPlanCase::getCaseId)
                .toList();
        if (caseIds.isEmpty()) {
            return List.of();
        }
        Map<Long, TestCase> caseMap = testCaseRepository.findAllById(caseIds).stream()
                .collect(Collectors.toMap(TestCase::getId, Function.identity()));
        return caseIds.stream()
                .map(caseMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, Object> calculateStats(List<Map<String, Object>> items) {
        long pass = items.stream().filter(i -> "PASS".equals(i.get("runStatus"))).count();
        long fail = items.stream().filter(i -> "FAIL".equals(i.get("runStatus"))).count();
        long blocked = items.stream().filter(i -> "BLOCKED".equals(i.get("runStatus"))).count();
        long skipped = items.stream().filter(i -> "SKIPPED".equals(i.get("runStatus"))).count();
        long executed = pass + fail + blocked + skipped;
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", items.size());
        stats.put("executed", executed);
        stats.put("untested", items.size() - executed);
        stats.put("pass", pass);
        stats.put("fail", fail);
        stats.put("blocked", blocked);
        stats.put("skipped", skipped);
        stats.put("passRate", executed == 0 ? 0.0 : round(pass * 100.0 / executed));
        return stats;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int clamp(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
