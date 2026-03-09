package org.example.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.AuditLogMapper;
import org.example.supermarket.entity.AuditLog;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    public AuditService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void log(String username, String role, String module, String action, String detail) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setRole(role);
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }

    public List<AuditLog> list(String username, String module, LocalDateTime start, LocalDateTime end, int limit) {
        QueryWrapper<AuditLog> q = new QueryWrapper<AuditLog>().orderByDesc("created_at").last("LIMIT " + Math.min(limit, 500));
        if (username != null && !username.isBlank()) q.eq("username", username.trim());
        if (module != null && !module.isBlank()) q.eq("module", module.trim());
        if (start != null) q.ge("created_at", start);
        if (end != null) q.le("created_at", end);
        return auditLogMapper.selectList(q);
    }
}
