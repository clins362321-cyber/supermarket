package org.example.supermarket.dto;

/**
 * 仓储发起采购：根据商品ID和采购数量创建采购申请
 */
public class WarehouseInitiateProcurementDto {
    private Long productId;
    private Integer qty;
    private Long supplierId;
    private String username;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
