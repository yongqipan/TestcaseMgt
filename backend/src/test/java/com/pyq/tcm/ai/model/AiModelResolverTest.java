package com.pyq.tcm.ai.model;

import com.pyq.tcm.ai.config.AiProperties;
import com.pyq.tcm.ai.crypto.AiCryptoService;
import com.pyq.tcm.ai.entity.AiModel;
import com.pyq.tcm.ai.entity.AiProvider;
import com.pyq.tcm.ai.repository.AiModelRepository;
import com.pyq.tcm.ai.repository.AiProviderRepository;
import com.pyq.tcm.common.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiModelResolverTest {

    @Mock
    private AiModelRepository modelRepository;

    @Mock
    private AiProviderRepository providerRepository;

    private AiCryptoService cryptoService;
    private AiModelResolver resolver;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        properties.setCryptoSecret("resolver-test-secret");
        properties.setDefaultBaseUrl("https://fallback.example/v1");
        cryptoService = new AiCryptoService(properties);
        resolver = new AiModelResolver(modelRepository, providerRepository, cryptoService, properties);
    }

    private AiModel model(long id, long providerId, boolean enabled) {
        AiModel model = new AiModel();
        model.setId(id);
        model.setProviderId(providerId);
        model.setName("model-" + id);
        model.setModelCode("code-" + id);
        model.setEnabled(enabled);
        return model;
    }

    private AiProvider provider(String apiKeyCipher, String baseUrl, boolean enabled) {
        AiProvider provider = new AiProvider();
        provider.setId(1L);
        provider.setName("provider");
        provider.setBaseUrl(baseUrl);
        provider.setApiKeyCipher(apiKeyCipher);
        provider.setEnabled(enabled);
        return provider;
    }

    @Test
    void resolvesRequestedModelWithDecryptedKey() {
        when(modelRepository.findById(10L)).thenReturn(Optional.of(model(10, 1, true)));
        when(providerRepository.findById(1L))
                .thenReturn(Optional.of(provider(cryptoService.encrypt("sk-key"), "https://api.test/v1", true)));

        ResolvedModel resolved = resolver.resolve(10L, "GENERATE_CASE");
        assertEquals(10L, resolved.getModelId());
        assertEquals("sk-key", resolved.getApiKey());
        assertEquals("https://api.test/v1", resolved.getBaseUrl());
    }

    @Test
    void fallsBackToDefaultBaseUrlWhenBlank() {
        when(modelRepository.findById(10L)).thenReturn(Optional.of(model(10, 1, true)));
        when(providerRepository.findById(1L))
                .thenReturn(Optional.of(provider(cryptoService.encrypt("sk-key"), "", true)));

        ResolvedModel resolved = resolver.resolve(10L, "GENERATE_CASE");
        assertEquals("https://fallback.example/v1", resolved.getBaseUrl());
    }

    @Test
    void resolvesSceneModelWhenNoRequestedId() {
        when(modelRepository.findBySceneAndEnabledTrueOrderByIdAsc("REVIEW_CASE"))
                .thenReturn(List.of(model(20, 1, true)));
        when(providerRepository.findById(1L))
                .thenReturn(Optional.of(provider(cryptoService.encrypt("sk-key"), "http://x", true)));

        ResolvedModel resolved = resolver.resolve(null, "REVIEW_CASE");
        assertEquals(20L, resolved.getModelId());
    }

    @Test
    void resolvesDefaultModelWhenSceneHasNone() {
        when(modelRepository.findBySceneAndEnabledTrueOrderByIdAsc("REVIEW_CASE")).thenReturn(List.of());
        when(modelRepository.findFirstByIsDefaultTrueAndEnabledTrue())
                .thenReturn(Optional.of(model(30, 1, true)));
        when(providerRepository.findById(1L))
                .thenReturn(Optional.of(provider(cryptoService.encrypt("sk-key"), "http://x", true)));

        assertEquals(30L, resolver.resolve(null, "REVIEW_CASE").getModelId());
    }

    @Test
    void rejectsDisabledModel() {
        when(modelRepository.findById(10L)).thenReturn(Optional.of(model(10, 1, false)));
        assertThrows(BizException.class, () -> resolver.resolve(10L, "GENERATE_CASE"));
    }

    @Test
    void rejectsDisabledProvider() {
        when(modelRepository.findById(10L)).thenReturn(Optional.of(model(10, 1, true)));
        when(providerRepository.findById(1L))
                .thenReturn(Optional.of(provider(cryptoService.encrypt("sk-key"), "http://x", false)));
        assertThrows(BizException.class, () -> resolver.resolve(10L, "GENERATE_CASE"));
    }

    @Test
    void rejectsMissingApiKey() {
        when(modelRepository.findById(10L)).thenReturn(Optional.of(model(10, 1, true)));
        when(providerRepository.findById(1L))
                .thenReturn(Optional.of(provider(null, "http://x", true)));
        assertThrows(BizException.class, () -> resolver.resolve(10L, "GENERATE_CASE"));
    }

    @Test
    void rejectsWhenNoModelConfigured() {
        when(modelRepository.findBySceneAndEnabledTrueOrderByIdAsc("REVIEW_CASE")).thenReturn(List.of());
        when(modelRepository.findFirstByIsDefaultTrueAndEnabledTrue()).thenReturn(Optional.empty());
        assertThrows(BizException.class, () -> resolver.resolve(null, "REVIEW_CASE"));
    }
}
