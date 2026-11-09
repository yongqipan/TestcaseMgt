package com.pyq.tcm.ai.admin;

import com.pyq.tcm.ai.dto.PromptRequest;
import com.pyq.tcm.ai.entity.AiPromptTemplate;
import com.pyq.tcm.ai.repository.AiPromptTemplateRepository;
import com.pyq.tcm.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AiPromptAdminService {

    private final AiPromptTemplateRepository templateRepository;

    public AiPromptAdminService(AiPromptTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<AiPromptTemplate> list() {
        return templateRepository.findAllByOrderByUpdatedAtDesc();
    }

    public List<AiPromptTemplate> listByScene(String scene) {
        return templateRepository.findBySceneOrderByVersionDesc(scene);
    }

    @Transactional
    public AiPromptTemplate create(PromptRequest request, String username) {
        validate(request);
        AiPromptTemplate template = new AiPromptTemplate();
        template.setScene(request.getScene().trim());
        template.setName(request.getName().trim());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        int nextVersion = templateRepository.findBySceneOrderByVersionDesc(request.getScene().trim()).stream()
                .mapToInt(AiPromptTemplate::getVersion)
                .max()
                .orElse(0) + 1;
        template.setVersion(nextVersion);
        template.setEnabled(request.getEnabled() == null || request.getEnabled());
        template.setUpdatedBy(username);
        return templateRepository.save(template);
    }

    @Transactional
    public AiPromptTemplate update(Long id, PromptRequest request, String username) {
        validate(request);
        AiPromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "Prompt 模板不存在"));
        template.setName(request.getName().trim());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        if (request.getEnabled() != null) {
            template.setEnabled(request.getEnabled());
        }
        template.setUpdatedBy(username);
        return templateRepository.save(template);
    }

    @Transactional
    public void delete(Long id) {
        AiPromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "Prompt 模板不存在"));
        templateRepository.delete(template);
    }

    private void validate(PromptRequest request) {
        if (request.getScene() == null || request.getScene().isBlank()) {
            throw new BizException("场景不能为空");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BizException("模板名称不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BizException("模板内容不能为空");
        }
    }
}
