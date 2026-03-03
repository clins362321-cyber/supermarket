package org.example.supermarket.order.dto;

import org.example.supermarket.order.Order;
import org.example.supermarket.order.OrderItem;

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

