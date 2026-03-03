package org.example.supermarket.user;

import jakarta.validation.Valid;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.user.dto.UserProfileDto;
import org.example.supermarket.user.dto.UserProfileUpdateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;

    public UserController(UserMapper userMapper, UserAddressMapper addressMapper) {
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
    }

    @GetMapping
    public List<User> list() {
        return userMapper.selectList(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user.setId(null);
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户注册：与 create 类似，但单独暴露一个注册接口，便于前端调用
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        user.setId(null);
        if (user.getUsername() == null || user.getUsername().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // 简单防止重复用户名
        Long cnt = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", user.getUsername())
        );
        if (cnt != null && cnt > 0) {
            return ResponseEntity.badRequest().build();
        }
        userMapper.insert(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User user) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        existing.setUsername(user.getUsername());
        existing.setPassword(user.getPassword());
        userMapper.updateById(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        int deleted = userMapper.deleteById(id);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据用户名获取个人资料和地址列表
     */
    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestParam("username") String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        List<UserAddress> addresses = addressMapper.selectList(
                new QueryWrapper<UserAddress>()
                        .eq("username", username)
                        .orderByDesc("is_default", "id")
        );
        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setBalance(user.getBalance());
        dto.setAddresses(addresses);
        return ResponseEntity.ok(dto);
    }

    /**
     * 更新个人资料（姓名、手机号）
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestParam("username") String username,
                                           @RequestBody UserProfileUpdateDto payload) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username)
        );
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setFullName(payload.getFullName());
        user.setPhone(payload.getPhone());
        userMapper.updateById(user);
        return ResponseEntity.ok().build();
    }

    /**
     * 新增或编辑地址
     */
    @PostMapping("/addresses")
    public ResponseEntity<?> saveAddress(@RequestParam("username") String username,
                                         @RequestBody UserAddress addr) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        addr.setUsername(username);
        if (addr.getId() == null) {
            addressMapper.insert(addr);
        } else {
            addressMapper.updateById(addr);
        }
        return ResponseEntity.ok(addr);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        int n = addressMapper.deleteById(id);
        if (n == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}

