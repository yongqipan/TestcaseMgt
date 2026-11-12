package com.pyq.tcm.ai.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyq.tcm.ai.client.AiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiJsonExtractorTest {

    private final AiJsonExtractor extractor = new AiJsonExtractor(new ObjectMapper());

    @Test
    void parsesPlainJsonArray() {
        JsonNode node = extractor.extract("[{\"title\":\"a\"}]");
        assertTrue(node.isArray());
        assertEquals("a", node.get(0).get("title").asText());
    }

    @Test
    void stripsMarkdownFence() {
        String raw = "```json\n{\"totalScore\": 90}\n```";
        JsonNode node = extractor.extract(raw);
        assertEquals(90, node.get("totalScore").asInt());
    }

    @Test
    void extractsJsonFromSurroundingProse() {
        String raw = "好的，结果如下：{\"riskLevel\":\"LOW\"} 以上。";
        JsonNode node = extractor.extract(raw);
        assertEquals("LOW", node.get("riskLevel").asText());
    }

    @Test
    void extractsNestedObjectFromText() {
        String raw = "prefix {\"a\":1,\"nested\":{\"b\":2}} suffix";
        JsonNode node = extractor.extract(raw);
        assertEquals(2, node.get("nested").get("b").asInt());
    }

    @Test
    void failsOnBlank() {
        assertThrows(AiException.class, () -> extractor.extract("   "));
        assertThrows(AiException.class, () -> extractor.extract(null));
    }

    @Test
    void failsOnInvalidJson() {
        AiException ex = assertThrows(AiException.class, () -> extractor.extract("{not json}"));
        assertEquals("INVALID_JSON", ex.getErrorCode());
    }
}
