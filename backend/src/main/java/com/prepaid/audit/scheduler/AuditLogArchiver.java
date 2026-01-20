package com.prepaid.audit.scheduler;

import com.prepaid.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 감사 로그 아카이빙 스케줄러
 * 매일 자정에 30일 이상 된 로그 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogArchiver {

    private final AuditLogRepository auditLogRepository;

    /**
     * 매일 자정 실행
     * 30일 이상 된 감사 로그 삭제
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void archiveOldLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        
        log.info("감사 로그 아카이빙 시작: cutoffDate={}", cutoffDate);
        
        try {
            int deletedCount = auditLogRepository.deleteOldLogs(cutoffDate);
            log.info("감사 로그 아카이빙 완료: 삭제된 로그 수={}", deletedCount);
        } catch (Exception e) {
            log.error("감사 로그 아카이빙 실패", e);
        }
    }
}
