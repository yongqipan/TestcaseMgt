package com.pyq.tcm.ai.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyq.tcm.entity.CaseStep;

import java.util.ArrayList;
import java.util.List;

final class StepsJsonCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StepsJsonCodec() {
    }

    static List<CaseStep> decode(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<CaseStep>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    static String encode(List<CaseStep> steps) {
        try {
            return MAPPER.writeValueAsString(steps == null ? new ArrayList<>() : steps);
        } catch (Exception e) {
            return "[]";
        }
    }
}
