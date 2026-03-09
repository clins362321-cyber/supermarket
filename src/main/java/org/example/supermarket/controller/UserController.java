package org.example.supermarket.controller;

import jakarta.validation.Valid;
import org.example.supermarket.dto.UserProfileDto;
import org.example.supermarket.dto.UserProfileUpdateDto;
import org.example.supermarket.entity.User;
import org.example.supermarket.entity.UserAddress;
import org.example.supermarket.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> list() {
        return userService.list();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        User user = userService.getById(id);
        return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        User registered = userService.register(user);
        return registered == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(registered);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User user) {
        User updated = userService.update(id, user);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestParam("username") String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        UserProfileDto dto = userService.getProfile(username);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestParam("username") String username,
                                           @RequestBody UserProfileUpdateDto payload) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        if (!userService.updateProfile(username, payload)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> saveAddress(@RequestParam("username") String username,
                                         @RequestBody UserAddress addr) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        UserAddress saved = userService.saveAddress(username, addr);
        return saved == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(saved);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        if (!userService.deleteAddress(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
