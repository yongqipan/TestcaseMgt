package com.pyq.tcm.ai.admin;

import com.pyq.tcm.ai.crypto.AiCryptoService;
import com.pyq.tcm.ai.dto.ProviderRequest;
import com.pyq.tcm.ai.entity.AiProvider;
import com.pyq.tcm.ai.repository.AiProviderRepository;
import com.pyq.tcm.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiProviderService {

    private final AiProviderRepository providerRepository;
    private final AiCryptoService cryptoService;

    public AiProviderService(AiProviderRepository providerRepository, AiCryptoService cryptoService) {
        this.providerRepository = providerRepository;
        this.cryptoService = cryptoService;
    }

    public List<Map<String, Object>> list() {
        return providerRepository.findAll().stream().map(this::toView).toList();
    }

    @Transactional
    public Map<String, Object> create(ProviderRequest request) {
        validate(request);
        if (providerRepository.findByName(request.getName().trim()).isPresent()) {
            throw new BizException("提供商名称已存在");
        }
        AiProvider provider = new AiProvider();
        apply(provider, request);
        return toView(providerRepository.save(provider));
    }

    @Transactional
    public Map<String, Object> update(Long id, ProviderRequest request) {
        validate(request);
        AiProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "提供商不存在"));
        providerRepository.findByName(request.getName().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BizException("提供商名称已存在");
                });
        apply(provider, request);
        return toView(providerRepository.save(provider));
    }

    @Transactional
    public void delete(Long id) {
        AiProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "提供商不存在"));
        providerRepository.delete(provider);
    }

    private void apply(AiProvider provider, ProviderRequest request) {
        provider.setName(request.getName().trim());
        provider.setBaseUrl(request.getBaseUrl());
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            provider.setApiKeyCipher(cryptoService.encrypt(request.getApiKey().trim()));
        }
        if (request.getEnabled() != null) {
            provider.setEnabled(request.getEnabled());
        }
    }

    private void validate(ProviderRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BizException("提供商名称不能为空");
        }
        if (request.getBaseUrl() == null || request.getBaseUrl().isBlank()) {
            throw new BizException("Base URL 不能为空");
        }
    }

    private Map<String, Object> toView(AiProvider provider) {
        String plain = cryptoService.decrypt(provider.getApiKeyCipher());
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", provider.getId());
        view.put("name", provider.getName());
        view.put("baseUrl", provider.getBaseUrl());
        view.put("apiKey", cryptoService.mask(plain));
        view.put("hasApiKey", plain != null && !plain.isBlank());
        view.put("enabled", provider.getEnabled());
        view.put("createdAt", provider.getCreatedAt());
        view.put("updatedAt", provider.getUpdatedAt());
        return view;
    }
}
