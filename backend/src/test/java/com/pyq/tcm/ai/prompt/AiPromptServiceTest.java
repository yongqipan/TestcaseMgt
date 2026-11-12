package com.pyq.tcm.ai.prompt;

import com.pyq.tcm.ai.entity.AiPromptTemplate;
import com.pyq.tcm.ai.repository.AiPromptTemplateRepository;
import com.pyq.tcm.common.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiPromptServiceTest {

    @Mock
    private AiPromptTemplateRepository templateRepository;

    private AiPromptTemplate template(int version, String content) {
        AiPromptTemplate template = new AiPromptTemplate();
        template.setScene("GENERATE_CASE");
        template.setVersion(version);
        template.setContent(content);
        return template;
    }

    @Test
    void rendersVariables() {
        when(templateRepository.findFirstBySceneAndEnabledTrueOrderByVersionDesc("GENERATE_CASE"))
                .thenReturn(Optional.of(template(2, "项目：{{projectName}}，需求：{{requirement}}")));
        AiPromptService service = new AiPromptService(templateRepository);

        Map<String, String> variables = new HashMap<>();
        variables.put("projectName", "电商");
        variables.put("requirement", "登录");

        AiPromptService.RenderedPrompt prompt = service.render("GENERATE_CASE", variables);
        assertEquals(2, prompt.version());
        assertEquals("项目：电商，需求：登录", prompt.text());
    }

    @Test
    void replacesNullVariableWithEmptyString() {
        when(templateRepository.findFirstBySceneAndEnabledTrueOrderByVersionDesc("GENERATE_CASE"))
                .thenReturn(Optional.of(template(1, "模块：{{moduleName}}")));
        AiPromptService service = new AiPromptService(templateRepository);

        Map<String, String> variables = new HashMap<>();
        variables.put("moduleName", null);

        assertEquals("模块：", service.render("GENERATE_CASE", variables).text());
    }

    @Test
    void keepsPlaceholderWhenVariableMissing() {
        when(templateRepository.findFirstBySceneAndEnabledTrueOrderByVersionDesc("GENERATE_CASE"))
                .thenReturn(Optional.of(template(1, "模块：{{moduleName}}")));
        AiPromptService service = new AiPromptService(templateRepository);

        assertEquals("模块：{{moduleName}}", service.render("GENERATE_CASE", Map.of()).text());
    }

    @Test
    void throwsWhenNoEnabledTemplate() {
        when(templateRepository.findFirstBySceneAndEnabledTrueOrderByVersionDesc("GENERATE_CASE"))
                .thenReturn(Optional.empty());
        AiPromptService service = new AiPromptService(templateRepository);
        assertThrows(BizException.class, () -> service.latestEnabled("GENERATE_CASE"));
    }
}
