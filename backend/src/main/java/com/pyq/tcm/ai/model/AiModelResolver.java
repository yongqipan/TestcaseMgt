package com.pyq.tcm.ai.model;

import com.pyq.tcm.ai.config.AiProperties;
import com.pyq.tcm.ai.crypto.AiCryptoService;
import com.pyq.tcm.ai.entity.AiModel;
import com.pyq.tcm.ai.entity.AiProvider;
import com.pyq.tcm.ai.repository.AiModelRepository;
import com.pyq.tcm.ai.repository.AiProviderRepository;
import com.pyq.tcm.common.BizException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiModelResolver {

    private final AiModelRepository modelRepository;
    private final AiProviderRepository providerRepository;
    private final AiCryptoService cryptoService;
    private final AiProperties properties;

    public AiModelResolver(AiModelRepository modelRepository,
                           AiProviderRepository providerRepository,
                           AiCryptoService cryptoService,
                           AiProperties properties) {
        this.modelRepository = modelRepository;
        this.providerRepository = providerRepository;
        this.cryptoService = cryptoService;
        this.properties = properties;
    }

    public ResolvedModel resolve(Long requestedModelId, String scene) {
        AiModel model = selectModel(requestedModelId, scene);
        AiProvider provider = providerRepository.findById(model.getProviderId())
                .orElseThrow(() -> new BizException("模型所属提供商不存在"));
        if (!Boolean.TRUE.equals(provider.getEnabled())) {
            throw new BizException("模型提供商已停用");
        }
        String apiKey = cryptoService.decrypt(provider.getApiKeyCipher());
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("模型提供商未配置有效的 API Key");
        }
        String baseUrl = provider.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = properties.getDefaultBaseUrl();
        }
        return new ResolvedModel(model.getId(), provider.getId(), model.getName(),
                model.getModelCode(), provider.getName(), baseUrl, apiKey,
                properties.getDefaultTemperature(), properties.getDefaultMaxTokens());
    }

    private AiModel selectModel(Long requestedModelId, String scene) {
        if (requestedModelId != null) {
            AiModel model = modelRepository.findById(requestedModelId)
                    .orElseThrow(() -> new BizException("指定的模型不存在"));
            if (!Boolean.TRUE.equals(model.getEnabled())) {
                throw new BizException("指定的模型已停用");
            }
            return model;
        }
        if (scene != null && !scene.isBlank()) {
            List<AiModel> sceneModels = modelRepository.findBySceneAndEnabledTrueOrderByIdAsc(scene);
            if (!sceneModels.isEmpty()) {
                return sceneModels.get(0);
            }
        }
        return modelRepository.findFirstByIsDefaultTrueAndEnabledTrue()
                .orElseThrow(() -> new BizException("未配置可用的默认 AI 模型"));
    }
}
