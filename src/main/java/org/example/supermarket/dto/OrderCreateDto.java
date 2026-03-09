package org.example.supermarket.dto;

import java.util.List;

public class OrderCreateDto {

    private String username;

    private List<OrderItemDto> items;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
}
