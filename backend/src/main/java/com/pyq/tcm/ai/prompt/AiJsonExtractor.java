package com.pyq.tcm.ai.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyq.tcm.ai.client.AiException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AiJsonExtractor {

    private static final Pattern FENCE = Pattern.compile("```(?:json)?\\s*(.+?)\\s*```", Pattern.DOTALL);

    private final ObjectMapper objectMapper;

    public AiJsonExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode extract(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new AiException("INVALID_JSON", "AI 返回内容为空，无法解析 JSON");
        }
        String text = raw.trim();
        Matcher matcher = FENCE.matcher(text);
        if (matcher.find()) {
            text = matcher.group(1).trim();
        }
        int objStart = text.indexOf('{');
        int arrStart = text.indexOf('[');
        int start = minPositive(objStart, arrStart);
        if (start >= 0) {
            char open = text.charAt(start);
            char close = open == '{' ? '}' : ']';
            int end = text.lastIndexOf(close);
            if (end > start) {
                text = text.substring(start, end + 1);
            }
        }
        try {
            return objectMapper.readTree(text);
        } catch (Exception e) {
            throw new AiException("INVALID_JSON", "AI 返回内容不是合法 JSON: " + e.getMessage());
        }
    }

    private int minPositive(int a, int b) {
        if (a < 0) {
            return b;
        }
        if (b < 0) {
            return a;
        }
        return Math.min(a, b);
    }
}
