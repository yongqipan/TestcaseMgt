package com.pyq.tcm.ai.admin;

import com.pyq.tcm.ai.dto.ModelRequest;
import com.pyq.tcm.ai.entity.AiModel;
import com.pyq.tcm.ai.entity.AiProvider;
import com.pyq.tcm.ai.repository.AiModelRepository;
import com.pyq.tcm.ai.repository.AiProviderRepository;
import com.pyq.tcm.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiModelAdminService {

    private final AiModelRepository modelRepository;
    private final AiProviderRepository providerRepository;

    public AiModelAdminService(AiModelRepository modelRepository, AiProviderRepository providerRepository) {
        this.modelRepository = modelRepository;
        this.providerRepository = providerRepository;
    }

    public List<Map<String, Object>> list() {
        Map<Long, String> providerNames = new LinkedHashMap<>();
        for (AiProvider provider : providerRepository.findAll()) {
            providerNames.put(provider.getId(), provider.getName());
        }
        return modelRepository.findAllByOrderByIdAsc().stream()
                .map(model -> toView(model, providerNames.get(model.getProviderId())))
                .toList();
    }

    @Transactional
    public Map<String, Object> create(ModelRequest request) {
        validate(request);
        AiModel model = new AiModel();
        apply(model, request);
        if (Boolean.TRUE.equals(model.getIsDefault())) {
            clearDefault(null);
        }
        AiModel saved = modelRepository.save(model);
        return toView(saved, providerName(saved.getProviderId()));
    }

    @Transactional
    public Map<String, Object> update(Long id, ModelRequest request) {
        validate(request);
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "模型不存在"));
        apply(model, request);
        if (Boolean.TRUE.equals(model.getIsDefault())) {
            clearDefault(id);
        }
        AiModel saved = modelRepository.save(model);
        return toView(saved, providerName(saved.getProviderId()));
    }

    @Transactional
    public void delete(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "模型不存在"));
        modelRepository.delete(model);
    }

    private void clearDefault(Long excludeId) {
        for (AiModel other : modelRepository.findAll()) {
            if (excludeId != null && other.getId().equals(excludeId)) {
                continue;
            }
            if (Boolean.TRUE.equals(other.getIsDefault())) {
                other.setIsDefault(Boolean.FALSE);
                modelRepository.save(other);
            }
        }
    }

    private void apply(AiModel model, ModelRequest request) {
        model.setProviderId(request.getProviderId());
        model.setName(request.getName().trim());
        model.setModelCode(request.getModelCode().trim());
        model.setScene(request.getScene());
        model.setContextWindow(request.getContextWindow());
        model.setInputPrice(request.getInputPrice());
        model.setOutputPrice(request.getOutputPrice());
        model.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        if (request.getEnabled() != null) {
            model.setEnabled(request.getEnabled());
        }
    }

    private void validate(ModelRequest request) {
        if (request.getProviderId() == null || !providerRepository.existsById(request.getProviderId())) {
            throw new BizException("提供商不存在");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BizException("模型名称不能为空");
        }
        if (request.getModelCode() == null || request.getModelCode().isBlank()) {
            throw new BizException("模型编码不能为空");
        }
    }

    private String providerName(Long providerId) {
        return providerRepository.findById(providerId).map(AiProvider::getName).orElse(null);
    }

    private Map<String, Object> toView(AiModel model, String providerName) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", model.getId());
        view.put("providerId", model.getProviderId());
        view.put("providerName", providerName);
        view.put("name", model.getName());
        view.put("modelCode", model.getModelCode());
        view.put("scene", model.getScene());
        view.put("contextWindow", model.getContextWindow());
        view.put("inputPrice", model.getInputPrice());
        view.put("outputPrice", model.getOutputPrice());
        view.put("isDefault", model.getIsDefault());
        view.put("enabled", model.getEnabled());
        view.put("createdAt", model.getCreatedAt());
        view.put("updatedAt", model.getUpdatedAt());
        return view;
    }
}
