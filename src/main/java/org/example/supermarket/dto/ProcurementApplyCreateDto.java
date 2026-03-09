package org.example.supermarket.dto;

import java.util.List;

public class ProcurementApplyCreateDto {
    private String title;
    private Long supplierId;
    private String username;
    private List<ProcurementApplyItemDto> items;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<ProcurementApplyItemDto> getItems() { return items; }
    public void setItems(List<ProcurementApplyItemDto> items) { this.items = items; }
}
