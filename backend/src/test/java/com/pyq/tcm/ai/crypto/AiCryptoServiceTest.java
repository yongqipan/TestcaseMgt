package com.pyq.tcm.ai.crypto;

import com.pyq.tcm.ai.config.AiProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiCryptoServiceTest {

    private AiCryptoService service(String secret, boolean allowInsecure) {
        AiProperties properties = new AiProperties();
        properties.setCryptoSecret(secret);
        properties.setAllowInsecureCrypto(allowInsecure);
        return new AiCryptoService(properties);
    }

    @Test
    void encryptThenDecryptRoundTrips() {
        AiCryptoService crypto = service("unit-test-secret", true);
        String cipher = crypto.encrypt("sk-abcdef123456");
        assertTrue(cipher.contains(":"));
        assertEquals("sk-abcdef123456", crypto.decrypt(cipher));
    }

    @Test
    void encryptionUsesRandomIv() {
        AiCryptoService crypto = service("unit-test-secret", true);
        String first = crypto.encrypt("same-value");
        String second = crypto.encrypt("same-value");
        assertFalse(first.equals(second));
    }

    @Test
    void decryptReturnsNullForMalformedInput() {
        AiCryptoService crypto = service("unit-test-secret", true);
        assertNull(crypto.decrypt("not-a-cipher"));
        assertNull(crypto.decrypt(null));
        assertNull(crypto.decrypt(""));
    }

    @Test
    void maskKeepsEndsAndHidesMiddle() {
        AiCryptoService crypto = service("unit-test-secret", true);
        assertEquals("sk-a****3456", crypto.mask("sk-abcdef123456"));
        assertEquals("****", crypto.mask("short"));
        assertNull(crypto.mask(null));
    }

    @Test
    void startupFailsWhenSecretMissingAndInsecureDisallowed() {
        assertThrows(IllegalStateException.class, () -> service("", false));
        assertThrows(IllegalStateException.class, () -> service("   ", false));
    }

    @Test
    void fallbackSecretAllowedWhenInsecureEnabled() {
        AiCryptoService crypto = service("", true);
        String cipher = crypto.encrypt("value");
        assertEquals("value", crypto.decrypt(cipher));
    }
}
