package com.pyq.tcm.service;

import com.pyq.tcm.entity.TestPlan;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import com.pyq.tcm.repository.TestPlanRepository;
import com.pyq.tcm.repository.TestRunRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestPlanRepository testPlanRepository;
    private final TestRunRepository testRunRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TestCaseRepository testCaseRepository,
                            TestPlanRepository testPlanRepository,
                            TestRunRepository testRunRepository) {
        this.projectRepository = projectRepository;
        this.testCaseRepository = testCaseRepository;
        this.testPlanRepository = testPlanRepository;
        this.testRunRepository = testRunRepository;
    }

    public Map<String, Object> stats() {
        long projectCount = projectRepository.count();
        long caseCount = testCaseRepository.count();
        long planCount = testPlanRepository.count();
        long runCount = testRunRepository.count();
        long pass = testRunRepository.countByStatus("PASS");
        double passRate = runCount == 0 ? 0.0 : Math.round(pass * 1000.0 / runCount) / 10.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectCount", projectCount);
        result.put("caseCount", caseCount);
        result.put("planCount", planCount);
        result.put("runCount", runCount);
        result.put("passRate", passRate);
        result.put("priorityDistribution", toDistribution(testCaseRepository.countGroupByPriority()));
        result.put("statusDistribution", toDistribution(testCaseRepository.countGroupByStatus()));

        List<Map<String, Object>> recentPlans = new ArrayList<>();
        for (TestPlan plan : testPlanRepository.findTop5ByOrderByCreatedAtDesc()) {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", plan.getId());
            view.put("name", plan.getName());
            view.put("projectId", plan.getProjectId());
            view.put("status", plan.getStatus());
            view.put("startDate", plan.getStartDate());
            view.put("endDate", plan.getEndDate());
            view.put("createdAt", plan.getCreatedAt());
            recentPlans.add(view);
        }
        result.put("recentPlans", recentPlans);
        return result;
    }

    private List<Map<String, Object>> toDistribution(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", row[0]);
            item.put("value", row[1]);
            result.add(item);
        }
        return result;
    }
}
