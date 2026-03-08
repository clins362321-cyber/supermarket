package org.example.supermarket.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求参数
 */
public class LoginRequest {

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 身份（admin / user）
     */
    @NotBlank(message = "登录身份不能为空")
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

