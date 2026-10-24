package com.pyq.tcm.config;

import com.pyq.tcm.entity.CaseStep;
import com.pyq.tcm.entity.Module;
import com.pyq.tcm.entity.Project;
import com.pyq.tcm.entity.TestCase;
import com.pyq.tcm.entity.TestPlan;
import com.pyq.tcm.entity.TestRun;
import com.pyq.tcm.entity.User;
import com.pyq.tcm.repository.ModuleRepository;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import com.pyq.tcm.repository.TestPlanRepository;
import com.pyq.tcm.repository.TestRunRepository;
import com.pyq.tcm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ModuleRepository moduleRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestPlanRepository testPlanRepository;
    private final TestRunRepository testRunRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProjectRepository projectRepository,
                           ModuleRepository moduleRepository,
                           TestCaseRepository testCaseRepository,
                           TestPlanRepository testPlanRepository,
                           TestRunRepository testRunRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.moduleRepository = moduleRepository;
        this.testCaseRepository = testCaseRepository;
        this.testPlanRepository = testPlanRepository;
        this.testRunRepository = testRunRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureAdmin();
        if (projectRepository.count() == 0) {
            seedDemoData();
            log.info("Demo data initialized");
        }
    }

    private void ensureAdmin() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User user = new User();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setNickname("系统管理员");
        user.setRole("ADMIN");
        userRepository.save(user);
        log.info("Default admin account created: admin / admin123");
    }

    private void seedDemoData() {
        Project project = new Project();
        project.setName("电商平台");
        project.setCode("SHOP");
        project.setDescription("覆盖用户中心、订单与支付的测试资产");
        project.setStatus("ACTIVE");
        project = projectRepository.save(project);

        Module userModule = saveModule(project.getId(), null, "用户中心", 1);
        Module orderModule = saveModule(project.getId(), null, "订单中心", 2);
        Module payModule = saveModule(project.getId(), orderModule.getId(), "支付", 1);
        saveModule(project.getId(), null, "商品中心", 3);

        TestCase loginCase = saveCase(project.getId(), userModule.getId(), "TC-SHOP-001",
                "使用正确密码登录", "P0", "FUNCTIONAL", "ACTIVE",
                "已存在状态正常的账号",
                List.of(new CaseStep("打开登录页，输入正确账号与密码", "输入框正常回显"),
                        new CaseStep("点击登录按钮", "跳转至工作台并展示用户昵称")),
                "登录,冒烟", "admin");

        TestCase wrongPasswordCase = saveCase(project.getId(), userModule.getId(), "TC-SHOP-002",
                "使用错误密码登录", "P0", "FUNCTIONAL", "ACTIVE",
                "已存在状态正常的账号",
                List.of(new CaseStep("输入正确账号与错误密码", "输入内容正常回显"),
                        new CaseStep("点击登录按钮", "提示用户名或密码错误，停留登录页")),
                "登录,异常", "admin");

        saveCase(project.getId(), orderModule.getId(), "TC-SHOP-003",
                "创建订单并校验库存扣减", "P1", "FUNCTIONAL", "ACTIVE",
                "商品库存大于 1，用户已登录",
                List.of(new CaseStep("选择商品加入购物车", "购物车数量加一"),
                        new CaseStep("提交订单", "订单创建成功，库存减一")),
                "订单,库存", "admin");

        saveCase(project.getId(), payModule.getId(), "TC-SHOP-004",
                "支付回调重复通知幂等", "P1", "API", "ACTIVE",
                "订单已支付成功",
                List.of(new CaseStep("重复发送支付成功回调", "接口返回成功"),
                        new CaseStep("查询订单状态", "订单状态保持已支付，未重复记账")),
                "支付,幂等", "admin");

        saveCase(project.getId(), payModule.getId(), "TC-SHOP-005",
                "支付超时自动关单", "P2", "FUNCTIONAL", "DRAFT",
                "订单创建后超过 30 分钟未支付",
                List.of(new CaseStep("等待超时任务执行", "订单状态变更为已关闭")),
                "支付,定时任务", "admin");

        saveCase(project.getId(), userModule.getId(), "TC-SHOP-006",
                "个人资料保存后刷新仍生效", "P2", "UI", "ACTIVE",
                "用户已登录",
                List.of(new CaseStep("修改昵称并保存", "提示保存成功"),
                        new CaseStep("刷新页面", "昵称展示为修改后的值")),
                "个人中心", "admin");

        TestPlan plan = new TestPlan();
        plan.setProjectId(project.getId());
        plan.setName("V1.2 版本回归测试");
        plan.setDescription("覆盖登录、下单与支付主流程");
        plan.setStatus("IN_PROGRESS");
        plan.setStartDate(LocalDate.now().minusDays(1));
        plan.setEndDate(LocalDate.now().plusDays(3));
        plan.setCreatedBy("admin");
        plan = testPlanRepository.save(plan);

        saveRun(plan.getId(), loginCase.getId(), "PASS", "admin", "主流程通过");
        saveRun(plan.getId(), wrongPasswordCase.getId(), "FAIL", "admin", "错误提示文案与需求不一致");
        saveRun(plan.getId(), payModuleCaseId(project.getId()), "PASS", "admin", "幂等校验通过");
    }

    private Module saveModule(Long projectId, Long parentId, String name, int sortOrder) {
        Module module = new Module();
        module.setProjectId(projectId);
        module.setParentId(parentId);
        module.setName(name);
        module.setSortOrder(sortOrder);
        return moduleRepository.save(module);
    }

    private TestCase saveCase(Long projectId, Long moduleId, String caseNo, String title,
                              String priority, String type, String status, String precondition,
                              List<CaseStep> steps, String tags, String creator) {
        TestCase testCase = new TestCase();
        testCase.setProjectId(projectId);
        testCase.setModuleId(moduleId);
        testCase.setCaseNo(caseNo);
        testCase.setTitle(title);
        testCase.setPriority(priority);
        testCase.setType(type);
        testCase.setStatus(status);
        testCase.setPrecondition(precondition);
        testCase.setSteps(steps);
        testCase.setTags(tags);
        testCase.setCreator(creator);
        testCase.setCreatedAt(LocalDateTime.now());
        return testCaseRepository.save(testCase);
    }

    private Long payModuleCaseId(Long projectId) {
        return testCaseRepository.findByProjectIdOrderByIdAsc(projectId).stream()
                .filter(c -> "TC-SHOP-004".equals(c.getCaseNo()))
                .map(TestCase::getId)
                .findFirst()
                .orElseThrow();
    }

    private void saveRun(Long planId, Long caseId, String status, String executor, String remark) {
        TestRun run = new TestRun();
        run.setPlanId(planId);
        run.setCaseId(caseId);
        run.setStatus(status);
        run.setExecutor(executor);
        run.setRemark(remark);
        run.setExecutedAt(LocalDateTime.now());
        testRunRepository.save(run);
    }
}
