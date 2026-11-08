package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiQuotaRepository extends JpaRepository<AiQuota, Long> {

    Optional<AiQuota> findByQuotaMonth(String quotaMonth);
}
