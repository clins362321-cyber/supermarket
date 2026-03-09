package org.example.supermarket.dto;

import org.example.supermarket.entity.Order;
import org.example.supermarket.entity.OrderItem;

import java.util.List;

public class OrderWithItemsDto {

    private Order order;
    private List<OrderItem> items;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
