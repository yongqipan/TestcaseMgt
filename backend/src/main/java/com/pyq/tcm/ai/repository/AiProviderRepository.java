package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiProviderRepository extends JpaRepository<AiProvider, Long> {

    Optional<AiProvider> findByName(String name);
}
