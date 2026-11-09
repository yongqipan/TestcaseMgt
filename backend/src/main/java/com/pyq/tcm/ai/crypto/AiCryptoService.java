package com.pyq.tcm.ai.crypto;

import com.pyq.tcm.ai.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AiCryptoService {

    private static final Logger log = LoggerFactory.getLogger(AiCryptoService.class);
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;
    private static final String SEPARATOR = ":";
    private static final String FALLBACK_SECRET = "tcm-ai-default-local-secret-change-me";

    private final SecretKeySpec keySpec;
    private final SecureRandom random = new SecureRandom();

    public AiCryptoService(AiProperties properties) {
        String secret = properties.getCryptoSecret();
        if (secret == null || secret.isBlank()) {
            log.warn("tcm.ai.crypto-secret 未配置，使用内置兜底密钥。生产环境请通过环境变量覆盖。");
            secret = FALLBACK_SECRET;
        }
        this.keySpec = new SecretKeySpec(sha256(secret), "AES");
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv)
                    + SEPARATOR
                    + Base64.getEncoder().encodeToString(cipherBytes);
        } catch (Exception e) {
            throw new IllegalStateException("加密 API Key 失败", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        try {
            String[] parts = cipherText.split(SEPARATOR, 2);
            if (parts.length != 2) {
                return null;
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherBytes = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("解密 API Key 失败: {}", e.getMessage());
            return null;
        }
    }

    public String mask(String plain) {
        if (plain == null || plain.isEmpty()) {
            return null;
        }
        if (plain.length() <= 8) {
            return "****";
        }
        return plain.substring(0, 4) + "****" + plain.substring(plain.length() - 4);
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("初始化加密密钥失败", e);
        }
    }
}
