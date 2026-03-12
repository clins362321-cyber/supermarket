package org.example.supermarket.controller;

import org.example.supermarket.dto.AfterSalesCreateDto;
import org.example.supermarket.entity.AfterSales;
import org.example.supermarket.service.AfterSalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/after-sales")
public class AfterSalesController {

    private final AfterSalesService afterSalesService;

    public AfterSalesController(AfterSalesService afterSalesService) {
        this.afterSalesService = afterSalesService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AfterSalesCreateDto dto) {
        try {
            AfterSales as = afterSalesService.create(dto);
            return ResponseEntity.ok(as);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-user")
    public List<AfterSales> listByUser(@RequestParam String username) {
        return afterSalesService.listByUser(username);
    }

    @GetMapping
    public List<AfterSales> listAll() {
        return afterSalesService.listAll();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            AfterSales as = afterSalesService.updateStatus(id, status);
            return ResponseEntity.ok(as);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
