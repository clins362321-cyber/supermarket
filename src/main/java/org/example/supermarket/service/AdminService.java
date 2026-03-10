package org.example.supermarket.service;

import org.example.supermarket.dao.AdminMapper;
import org.example.supermarket.entity.Admin;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminMapper adminMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Admin> list() {
        return adminMapper.selectList(null);
    }

    public Admin getById(Long id) {
        return adminMapper.selectById(id);
    }

    public Admin create(Admin admin) {
        admin.setId(null);
        if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        if (admin.getRole() == null || admin.getRole().isBlank()) {
            admin.setRole("ADMIN");
        }
        adminMapper.insert(admin);
        return admin;
    }

    public Admin update(Long id, Admin admin) {
        Admin existing = adminMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        existing.setUsername(admin.getUsername());
        if (admin.getRole() != null && !admin.getRole().isBlank()) {
            existing.setRole(admin.getRole());
        }
        if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        adminMapper.updateById(existing);
        return existing;
    }

    public boolean delete(Long id) {
        return adminMapper.deleteById(id) > 0;
    }
}
