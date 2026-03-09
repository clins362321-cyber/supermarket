package org.example.supermarket.controller;

import org.example.supermarket.dto.ProcurementApplyCreateDto;
import org.example.supermarket.entity.ProcurementApply;
import org.example.supermarket.entity.ProcurementApplyItem;
import org.example.supermarket.entity.Supplier;
import org.example.supermarket.service.AuditService;
import org.example.supermarket.service.ProcurementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procurement")
public class ProcurementController {

    private final ProcurementService procurementService;
    private final AuditService auditService;

    public ProcurementController(ProcurementService procurementService, AuditService auditService) {
        this.procurementService = procurementService;
        this.auditService = auditService;
    }

    @GetMapping("/suppliers")
    public List<Supplier> listSuppliers() {
        return procurementService.listSuppliers();
    }

    @PostMapping("/suppliers")
    public ResponseEntity<Supplier> createSupplier(@RequestBody Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Supplier s = procurementService.createSupplier(supplier);
        auditService.log(null, null, "procurement", "create_supplier", "供应商: " + s.getName());
        return ResponseEntity.ok(s);
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        Supplier updated = procurementService.updateSupplier(supplier);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @GetMapping("/applies")
    public List<ProcurementApply> listApplies(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String username) {
        return procurementService.listApplies(status, username);
    }

    @GetMapping("/applies/pending")
    public List<ProcurementApply> listPendingApprovals() {
        return procurementService.listPendingApprovals();
    }

    @PostMapping("/applies")
    public ResponseEntity<ProcurementApply> createApply(@RequestBody ProcurementApplyCreateDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            dto.setTitle("采购申请");
        }
        ProcurementApply apply = procurementService.createApplyWithItems(dto);
        auditService.log(dto.getUsername(), null, "procurement", "create_apply", "申请单: " + apply.getApplyNo());
        return ResponseEntity.ok(apply);
    }

    @PutMapping("/applies/{id}/approve")
    public ResponseEntity<ProcurementApply> approve(@PathVariable Long id, @RequestParam String approver) {
        try {
            ProcurementApply apply = procurementService.approve(id, approver);
            auditService.log(approver, null, "procurement", "approve", "申请单ID: " + id);
            return ResponseEntity.ok(apply);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/applies/{id}/reject")
    public ResponseEntity<ProcurementApply> reject(@PathVariable Long id, @RequestParam String approver) {
        try {
            ProcurementApply apply = procurementService.reject(id, approver);
            auditService.log(approver, null, "procurement", "reject", "申请单ID: " + id);
            return ResponseEntity.ok(apply);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/applies/{id}/items")
    public List<ProcurementApplyItem> listApplyItems(@PathVariable Long id) {
        return procurementService.listApplyItems(id);
    }
}
