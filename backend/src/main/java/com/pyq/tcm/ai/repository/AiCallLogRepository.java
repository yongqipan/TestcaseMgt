package com.pyq.tcm.ai.repository;

import com.pyq.tcm.ai.entity.AiCallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AiCallLogRepository extends JpaRepository<AiCallLog, Long> {

    @Query("select l from AiCallLog l where " +
            "(:scene = '' or l.scene = :scene) " +
            "and (:modelName = '' or l.modelName = :modelName) " +
            "and (:status = '' or l.status = :status) " +
            "and (:username = '' or l.username = :username)")
    Page<AiCallLog> search(@Param("scene") String scene,
                           @Param("modelName") String modelName,
                           @Param("status") String status,
                           @Param("username") String username,
                           Pageable pageable);

    @Query("select l.scene, sum(l.totalTokens) from AiCallLog l " +
            "where l.createdAt between :from and :to group by l.scene")
    List<Object[]> sumTokensByScene(@Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    @Query("select sum(l.totalTokens) from AiCallLog l where l.createdAt between :from and :to")
    Long sumTokensBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select count(l), avg(l.latencyMs) from AiCallLog l where l.createdAt between :from and :to")
    Object[] summaryBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
