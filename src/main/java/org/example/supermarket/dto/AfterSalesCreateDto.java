package org.example.supermarket.dto;

public class AfterSalesCreateDto {
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String username;
    private String reason;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
