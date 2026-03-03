package org.example.supermarket.auth;

/**
 * 登录响应结果
 */
public class LoginResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 登录身份（admin / user）
     */
    private String role;

    /**
     * 简单 token（示例，后续可以替换为 JWT）
     */
    private String token;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static LoginResponse ok(String role, String token) {
        LoginResponse r = new LoginResponse();
        r.setSuccess(true);
        r.setRole(role);
        r.setToken(token);
        r.setMessage("登录成功");
        return r;
    }

    public static LoginResponse fail(String message) {
        LoginResponse r = new LoginResponse();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }
}

