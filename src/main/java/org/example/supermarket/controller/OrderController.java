package org.example.supermarket.controller;

import org.example.supermarket.dto.OrderCreateDto;
import org.example.supermarket.dto.OrderWithItemsDto;
import org.example.supermarket.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrderCreateDto dto) {
        try {
            Long orderId = orderService.createOrder(dto);
            return ResponseEntity.ok(orderId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-user")
    public ResponseEntity<?> listByUser(@RequestParam("username") String username) {
        try {
            List<OrderWithItemsDto> result = orderService.listByUser(username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
