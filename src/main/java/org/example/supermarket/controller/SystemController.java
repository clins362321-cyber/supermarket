package org.example.supermarket.controller;

import org.example.supermarket.entity.AuditLog;
import org.example.supermarket.service.AuditService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final AuditService auditService;

    public SystemController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> listAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "100") int limit) {
        LocalDateTime startDt = parseDateTime(start);
        LocalDateTime endDt = parseDateTime(end);
        return auditService.list(username, module, startDt, endDt, limit);
    }

    @GetMapping("/roles")
    public List<Map<String, String>> listRoles() {
        return List.of(
                Map.of("code", "ADMIN", "name", "超级管理员", "desc", "全部权限"),
                Map.of("code", "APPROVER", "name", "采购审批员", "desc", "审批采购申请"),
                Map.of("code", "WAREHOUSE", "name", "仓储管理员", "desc", "仓储、发起采购")
        );
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(s + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
