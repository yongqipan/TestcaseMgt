package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiPromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiPromptTemplateRepository extends JpaRepository<AiPromptTemplate, Long> {

    List<AiPromptTemplate> findAllByOrderByUpdatedAtDesc();

    List<AiPromptTemplate> findBySceneOrderByVersionDesc(String scene);

    Optional<AiPromptTemplate> findFirstBySceneAndEnabledTrueOrderByVersionDesc(String scene);
}
