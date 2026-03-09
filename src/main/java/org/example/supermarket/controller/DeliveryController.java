package org.example.supermarket.controller;

import org.example.supermarket.dto.DeliveryOrderDto;
import org.example.supermarket.entity.DeliveryPerson;
import org.example.supermarket.entity.DeliveryRoute;
import org.example.supermarket.entity.UserAddress;
import org.example.supermarket.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配送管理接口：订单/配送单、配送员、路线、状态流转
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<DeliveryOrderDto>> listOrders() {
        return ResponseEntity.ok(deliveryService.listOrders());
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<UserAddress>> listAddresses(@RequestParam("username") String username) {
        try {
            List<UserAddress> list = deliveryService.listAddresses(username);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/persons")
    public ResponseEntity<List<DeliveryPerson>> listPersons() {
        return ResponseEntity.ok(deliveryService.listPersons());
    }

    @GetMapping("/routes")
    public ResponseEntity<List<DeliveryRoute>> listRoutes() {
        return ResponseEntity.ok(deliveryService.listRoutes());
    }

    @PostMapping("/persons")
    public ResponseEntity<?> createPerson(@RequestBody DeliveryPerson person) {
        try {
            DeliveryPerson created = deliveryService.createPerson(person);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/routes")
    public ResponseEntity<?> createRoute(@RequestBody DeliveryRoute route) {
        try {
            DeliveryRoute created = deliveryService.createRoute(route);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/orders/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable Long id,
                                    @RequestParam(required = false) Long personId,
                                    @RequestParam(required = false) Long routeId,
                                    @RequestParam(required = false) Long addressId) {
        if (!deliveryService.assignOrder(id, personId, routeId, addressId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestParam String status,
                                          @RequestParam(required = false) String remark) {
        try {
            if (!deliveryService.updateOrderStatus(id, status, remark)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
