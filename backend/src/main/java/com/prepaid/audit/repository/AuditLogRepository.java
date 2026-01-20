package com.prepaid.audit.repository;

import com.prepaid.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 Repository
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * 사용자별 최근 감사 로그 조회
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * 30일 이상 된 데이터 삭제 (아카이빙용)
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}
