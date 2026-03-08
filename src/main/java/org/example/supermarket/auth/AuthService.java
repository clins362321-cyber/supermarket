package org.example.supermarket.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.admin.Admin;
import org.example.supermarket.admin.AdminMapper;
import org.example.supermarket.user.User;
import org.example.supermarket.user.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * 登录校验服务（基于 MyBatis-Plus 查询 admin / user 表，密码使用 BCrypt 哈希比对）
 */
@Service
public class AuthService {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AdminMapper adminMapper, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 校验账号密码，并返回登录结果
     */
    public LoginResponse login(LoginRequest request) {
        String role = request.getRole();
        String username = request.getUsername();
        String password = request.getPassword();

        if ("admin".equalsIgnoreCase(role)) {
            Admin admin = adminMapper.selectOne(
                    new QueryWrapper<Admin>().eq("username", username)
            );
            if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
                return LoginResponse.ok("admin", generateToken(username, "admin"));
            }
            return LoginResponse.fail("管理员账号或密码错误");
        } else if ("user".equalsIgnoreCase(role)) {
            User user = userMapper.selectOne(
                    new QueryWrapper<User>().eq("username", username)
            );
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                return LoginResponse.ok("user", generateToken(username, "user"));
            }
            return LoginResponse.fail("用户账号或密码错误");
        }

        return LoginResponse.fail("不支持的登录身份：" + role);
    }

    /**
     * 生成一个非常简单的 token（仅示例用，非安全方案）
     */
    private String generateToken(String username, String role) {
        String raw = username + ":" + role + ":" + Instant.now().toEpochMilli();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}

