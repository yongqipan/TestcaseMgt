package com.pyq.tcm.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class StepsConverter implements AttributeConverter<List<CaseStep>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<CaseStep> attribute) {
        try {
            if (attribute == null) {
                return "[]";
            }
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("测试步骤序列化失败", e);
        }
    }

    @Override
    public List<CaseStep> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new ArrayList<>();
            }
            return MAPPER.readValue(dbData, new TypeReference<List<CaseStep>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
