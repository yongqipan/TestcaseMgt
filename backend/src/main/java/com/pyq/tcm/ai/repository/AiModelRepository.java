package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiModelRepository extends JpaRepository<AiModel, Long> {

    List<AiModel> findByProviderIdOrderByIdAsc(Long providerId);

    List<AiModel> findAllByOrderByIdAsc();

    List<AiModel> findBySceneAndEnabledTrueOrderByIdAsc(String scene);

    List<AiModel> findByEnabledTrueOrderByIdAsc();

    Optional<AiModel> findFirstByIsDefaultTrueAndEnabledTrue();
}
