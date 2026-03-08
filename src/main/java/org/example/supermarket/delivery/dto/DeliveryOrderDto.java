package org.example.supermarket.delivery.dto;

import org.example.supermarket.order.Order;
import org.example.supermarket.order.OrderItem;
import org.example.supermarket.user.UserAddress;

import java.util.List;

/**
 * 配送管理用订单 DTO，包含订单、明细、地址、配送员、路线
 */
public class DeliveryOrderDto {

    private Order order;
    private List<OrderItem> items;
    private UserAddress address;
    private String deliveryPersonName;
    private String deliveryRouteName;

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public UserAddress getAddress() { return address; }
    public void setAddress(UserAddress address) { this.address = address; }
    public String getDeliveryPersonName() { return deliveryPersonName; }
    public void setDeliveryPersonName(String deliveryPersonName) { this.deliveryPersonName = deliveryPersonName; }
    public String getDeliveryRouteName() { return deliveryRouteName; }
    public void setDeliveryRouteName(String deliveryRouteName) { this.deliveryRouteName = deliveryRouteName; }
}
