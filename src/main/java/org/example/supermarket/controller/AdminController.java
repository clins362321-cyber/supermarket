package org.example.supermarket.controller;

import jakarta.validation.Valid;
import org.example.supermarket.entity.Admin;
import org.example.supermarket.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@Validated
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<Admin> list() {
        return adminService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> get(@PathVariable Long id) {
        Admin admin = adminService.getById(id);
        return admin == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(admin);
    }

    @PostMapping
    public Admin create(@Valid @RequestBody Admin admin) {
        return adminService.create(admin);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> update(@PathVariable Long id, @Valid @RequestBody Admin admin) {
        Admin updated = adminService.update(id, admin);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!adminService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
