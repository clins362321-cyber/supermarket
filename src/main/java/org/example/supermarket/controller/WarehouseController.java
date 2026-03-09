package org.example.supermarket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.ProductMapper;
import org.example.supermarket.dto.WarehouseInitiateProcurementDto;
import org.example.supermarket.entity.ProcurementApply;
import org.example.supermarket.entity.Product;
import org.example.supermarket.service.AuditService;
import org.example.supermarket.service.ProcurementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private final ProductMapper productMapper;
    private final ProcurementService procurementService;
    private final AuditService auditService;

    public WarehouseController(ProductMapper productMapper, ProcurementService procurementService, AuditService auditService) {
        this.productMapper = productMapper;
        this.procurementService = procurementService;
        this.auditService = auditService;
    }

    @GetMapping("/inventory")
    public List<Product> listInventory() {
        return productMapper.selectList(new QueryWrapper<Product>().orderByAsc("id"));
    }

    @GetMapping("/alerts")
    public List<Product> listAlerts(@RequestParam(defaultValue = "50") int threshold) {
        List<Product> all = productMapper.selectList(null);
        return all.stream()
                .filter(p -> {
                    int stock = p.getStock() == null ? 0 : p.getStock();
                    int safe = p.getSafeStock() != null ? p.getSafeStock() : threshold;
                    return stock < safe;
                })
                .collect(Collectors.toList());
    }

    @PutMapping("/products/{id}/safe-stock")
    public ResponseEntity<?> updateSafeStock(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Product p = productMapper.selectById(id);
        if (p == null) return ResponseEntity.notFound().build();
        Object val = body != null ? body.get("safeStock") : null;
        int safeStock;
        if (val instanceof Number) {
            safeStock = ((Number) val).intValue();
        } else if (val != null && !val.toString().isBlank()) {
            try {
                safeStock = Integer.parseInt(val.toString().trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("安全库存必须为非负整数");
            }
        } else {
            return ResponseEntity.badRequest().body("安全库存必须为非负整数");
        }
        if (safeStock < 0) {
            return ResponseEntity.badRequest().body("安全库存必须为非负整数");
        }
        p.setSafeStock(safeStock);
        productMapper.updateById(p);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/initiate-procurement")
    public ResponseEntity<?> initiateProcurement(@RequestBody WarehouseInitiateProcurementDto dto) {
        try {
            ProcurementApply apply = procurementService.createApplyFromWarehouse(dto);
            auditService.log(dto.getUsername(), null, "warehouse", "initiate_procurement", "商品ID: " + dto.getProductId() + ", 申请单: " + apply.getApplyNo());
            return ResponseEntity.ok(apply);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
