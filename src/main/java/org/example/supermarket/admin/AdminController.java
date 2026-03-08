package org.example.supermarket.admin;

import jakarta.validation.Valid;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@Validated
public class AdminController {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminMapper adminMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<Admin> list() {
        return adminMapper.selectList(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> get(@PathVariable Long id) {
        Admin admin = adminMapper.selectById(id);
        return admin == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(admin);
    }

    @PostMapping
    public Admin create(@Valid @RequestBody Admin admin) {
        admin.setId(null);
        if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        adminMapper.insert(admin);
        return admin;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> update(@PathVariable Long id, @Valid @RequestBody Admin admin) {
        Admin existing = adminMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setUsername(admin.getUsername());
        if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        adminMapper.updateById(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        int deleted = adminMapper.deleteById(id);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

