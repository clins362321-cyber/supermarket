package org.example.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 下单用户名（与 user.username 对应）
     */
    private String username;

    /**
     * 订单总价
     */
    private BigDecimal totalPrice;

    /**
     * 下单时间
     */
    private LocalDateTime createdAt;

    /**
     * 收货地址ID
     */
    private Long addressId;

    /**
     * 配送状态：PENDING_ACCEPT(待接单)、IN_DELIVERY(配送中)、DELIVERED(已签收)、EXCEPTION(异常)
     */
    private String deliveryStatus;

    /**
     * 配送员ID
     */
    private Long deliveryPersonId;

    /**
     * 配送路线ID
     */
    private Long deliveryRouteId;

    /**
     * 配送备注（异常时填写）
     */
    private String deliveryRemark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public Long getDeliveryPersonId() { return deliveryPersonId; }
    public void setDeliveryPersonId(Long deliveryPersonId) { this.deliveryPersonId = deliveryPersonId; }
    public Long getDeliveryRouteId() { return deliveryRouteId; }
    public void setDeliveryRouteId(Long deliveryRouteId) { this.deliveryRouteId = deliveryRouteId; }
    public String getDeliveryRemark() { return deliveryRemark; }
    public void setDeliveryRemark(String deliveryRemark) { this.deliveryRemark = deliveryRemark; }
}
