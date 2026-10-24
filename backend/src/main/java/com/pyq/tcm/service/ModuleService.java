package com.pyq.tcm.service;

import com.pyq.tcm.common.BizException;
import com.pyq.tcm.entity.Module;
import com.pyq.tcm.repository.ModuleRepository;
import com.pyq.tcm.repository.ProjectRepository;
import com.pyq.tcm.repository.TestCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ProjectRepository projectRepository;
    private final TestCaseRepository testCaseRepository;

    public ModuleService(ModuleRepository moduleRepository,
                         ProjectRepository projectRepository,
                         TestCaseRepository testCaseRepository) {
        this.moduleRepository = moduleRepository;
        this.projectRepository = projectRepository;
        this.testCaseRepository = testCaseRepository;
    }

    public List<Map<String, Object>> tree(Long projectId) {
        if (projectId == null) {
            throw new BizException("请指定项目");
        }
        List<Module> modules = moduleRepository.findByProjectIdOrderBySortOrderAscIdAsc(projectId);
        Map<Long, Map<String, Object>> nodes = new LinkedHashMap<>();
        for (Module module : modules) {
            nodes.put(module.getId(), toNode(module));
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Module module : modules) {
            Map<String, Object> node = nodes.get(module.getId());
            Map<String, Object> parent = module.getParentId() == null ? null : nodes.get(module.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                childrenOf(parent).add(node);
            }
        }
        sortRecursively(roots);
        return roots;
    }

    @Transactional
    public Module create(Module module) {
        module.setId(null);
        if (module.getProjectId() == null || !projectRepository.existsById(module.getProjectId())) {
            throw new BizException("项目不存在");
        }
        if (module.getParentId() != null && !moduleRepository.existsById(module.getParentId())) {
            throw new BizException("父模块不存在");
        }
        module.setName(module.getName().trim());
        return moduleRepository.save(module);
    }

    @Transactional
    public Module update(Long id, Module payload) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "模块不存在"));
        if (payload.getParentId() != null) {
            if (payload.getParentId().equals(id)) {
                throw new BizException("父模块不能是自身");
            }
            if (!moduleRepository.existsById(payload.getParentId())) {
                throw new BizException("父模块不存在");
            }
        }
        module.setName(payload.getName().trim());
        module.setSortOrder(payload.getSortOrder());
        module.setParentId(payload.getParentId());
        return moduleRepository.save(module);
    }

    @Transactional
    public void delete(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "模块不存在"));
        if (moduleRepository.existsByParentId(id)) {
            throw new BizException("模块下存在子模块，请先删除子模块");
        }
        if (testCaseRepository.countByModuleId(id) > 0) {
            throw new BizException("模块下存在测试用例，请先删除用例");
        }
        moduleRepository.delete(module);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> childrenOf(Map<String, Object> node) {
        return (List<Map<String, Object>>) node.get("children");
    }

    private Map<String, Object> toNode(Module module) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", module.getId());
        node.put("projectId", module.getProjectId());
        node.put("parentId", module.getParentId());
        node.put("name", module.getName());
        node.put("sortOrder", module.getSortOrder());
        node.put("children", new ArrayList<Map<String, Object>>());
        return node;
    }

    @SuppressWarnings("unchecked")
    private void sortRecursively(List<Map<String, Object>> nodes) {
        nodes.sort(Comparator.comparingInt(n -> {
            Object order = n.get("sortOrder");
            return order instanceof Integer ? (Integer) order : 0;
        }));
        for (Map<String, Object> node : nodes) {
            List<Map<String, Object>> children = childrenOf(node);
            if (!children.isEmpty()) {
                sortRecursively(children);
            }
        }
    }
}
