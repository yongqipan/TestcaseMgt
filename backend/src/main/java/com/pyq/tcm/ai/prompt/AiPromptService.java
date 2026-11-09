package com.pyq.tcm.ai.prompt;

import com.pyq.tcm.ai.entity.AiPromptTemplate;
import com.pyq.tcm.ai.repository.AiPromptTemplateRepository;
import com.pyq.tcm.common.BizException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiPromptService {

    private final AiPromptTemplateRepository templateRepository;

    public AiPromptService(AiPromptTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public AiPromptTemplate latestEnabled(String scene) {
        return templateRepository.findFirstBySceneAndEnabledTrueOrderByVersionDesc(scene)
                .orElseThrow(() -> new BizException("未找到场景 " + scene + " 的可用 Prompt 模板"));
    }

    public RenderedPrompt render(String scene, Map<String, String> variables) {
        AiPromptTemplate template = latestEnabled(scene);
        String content = template.getContent() == null ? "" : template.getContent();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String value = entry.getValue() == null ? "" : entry.getValue();
                content = content.replace("{{" + entry.getKey() + "}}", value);
            }
        }
        return new RenderedPrompt(template.getVersion(), content);
    }

    public record RenderedPrompt(Integer version, String text) {
    }
}
