package com.pyq.tcm.config;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private static final long TTL_HOURS = 12;

    public record TokenInfo(Long userId, String username, LocalDateTime expiresAt) {
    }

    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public String issue(Long userId, String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, new TokenInfo(userId, username, LocalDateTime.now().plusHours(TTL_HOURS)));
        cleanupExpired();
        return token;
    }

    public TokenInfo validate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        TokenInfo info = tokens.get(token);
        if (info == null) {
            return null;
        }
        if (info.expiresAt().isBefore(LocalDateTime.now())) {
            tokens.remove(token);
            return null;
        }
        return info;
    }

    public void revoke(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        tokens.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
