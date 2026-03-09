package org.example.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("procurement_apply")
public class ProcurementApply {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String applyNo;
    private String title;
    private Long supplierId;
    private String status;
    private String username;
    private String approverUsername;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApplyNo() { return applyNo; }
    public void setApplyNo(String applyNo) { this.applyNo = applyNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getApproverUsername() { return approverUsername; }
    public void setApproverUsername(String approverUsername) { this.approverUsername = approverUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
