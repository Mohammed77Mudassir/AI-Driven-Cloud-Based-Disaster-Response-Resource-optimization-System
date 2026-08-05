package com.disaster.service;

import com.disaster.dto.AuditLogDTO;
import com.disaster.entity.AuditLog;
import com.disaster.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    public void log(String action, String entityType, Long entityId, String performedBy, String details) {
        AuditLog log = AuditLog.builder()
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .performedBy(performedBy)
            .details(details)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(log);
    }
    
    public List<AuditLogDTO> getAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
            .stream().map(AuditLogDTO::fromEntity).toList();
    }
    
    public List<AuditLogDTO> getByUser(String username) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(username)
            .stream().map(AuditLogDTO::fromEntity).toList();
    }
}
