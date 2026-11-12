package com.pyq.tcm.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenStoreTest {

    @Test
    void issuesAndValidatesToken() {
        TokenStore store = new TokenStore();
        String token = store.issue(1L, "admin", "ADMIN");
        TokenStore.TokenInfo info = store.validate(token);
        assertNotNull(info);
        assertEquals(1L, info.userId());
        assertEquals("admin", info.username());
        assertEquals("ADMIN", info.role());
    }

    @Test
    void validatesInvalidTokenAsNull() {
        TokenStore store = new TokenStore();
        assertNull(store.validate("missing"));
        assertNull(store.validate(""));
        assertNull(store.validate(null));
    }

    @Test
    void revokeInvalidatesToken() {
        TokenStore store = new TokenStore();
        String token = store.issue(1L, "admin", "ADMIN");
        store.revoke(token);
        assertNull(store.validate(token));
    }

    @Test
    void issuesUniqueTokens() {
        TokenStore store = new TokenStore();
        String a = store.issue(1L, "admin", "ADMIN");
        String b = store.issue(1L, "admin", "ADMIN");
        assertEquals(32, a.length());
        assertEquals(false, a.equals(b));
    }
}
