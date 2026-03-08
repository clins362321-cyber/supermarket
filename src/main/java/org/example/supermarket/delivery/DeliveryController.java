package org.example.supermarket.delivery;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.delivery.dto.DeliveryOrderDto;
import org.example.supermarket.order.Order;
import org.example.supermarket.order.OrderItem;
import org.example.supermarket.order.mapper.OrderItemMapper;
import org.example.supermarket.order.mapper.OrderMapper;
import org.example.supermarket.user.UserAddress;
import org.example.supermarket.user.UserAddressMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配送管理接口：订单/配送单、配送员、路线、状态流转
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserAddressMapper addressMapper;
    private final DeliveryPersonMapper deliveryPersonMapper;
    private final DeliveryRouteMapper deliveryRouteMapper;

    public DeliveryController(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                              UserAddressMapper addressMapper, DeliveryPersonMapper deliveryPersonMapper,
                              DeliveryRouteMapper deliveryRouteMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.addressMapper = addressMapper;
        this.deliveryPersonMapper = deliveryPersonMapper;
        this.deliveryRouteMapper = deliveryRouteMapper;
    }

    /**
     * 配送订单列表（含明细、地址、配送员、路线）
     */
    @GetMapping("/orders")
    public ResponseEntity<List<DeliveryOrderDto>> listOrders() {
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().orderByDesc("created_at", "id"));
        if (orders.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().in("order_id", orderIds));

        List<Long> addressIds = orders.stream()
                .map(Order::getAddressId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, UserAddress> addressMap = addressIds.isEmpty() ? Map.of() :
                addressMapper.selectBatchIds(addressIds).stream().collect(Collectors.toMap(UserAddress::getId, a -> a));

        List<Long> personIds = orders.stream()
                .map(Order::getDeliveryPersonId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, DeliveryPerson> personMap = personIds.isEmpty() ? Map.of() :
                deliveryPersonMapper.selectBatchIds(personIds).stream().collect(Collectors.toMap(DeliveryPerson::getId, p -> p));

        List<Long> routeIds = orders.stream()
                .map(Order::getDeliveryRouteId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, DeliveryRoute> routeMap = routeIds.isEmpty() ? Map.of() :
                deliveryRouteMapper.selectBatchIds(routeIds).stream().collect(Collectors.toMap(DeliveryRoute::getId, r -> r));

        Map<Long, List<OrderItem>> itemsByOrder = items.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<DeliveryOrderDto> result = new ArrayList<>();
        for (Order order : orders) {
            DeliveryOrderDto dto = new DeliveryOrderDto();
            dto.setOrder(order);
            dto.setItems(itemsByOrder.getOrDefault(order.getId(), List.of()));
            dto.setAddress(order.getAddressId() != null ? addressMap.get(order.getAddressId()) : null);
            dto.setDeliveryPersonName(order.getDeliveryPersonId() != null && personMap.containsKey(order.getDeliveryPersonId())
                    ? personMap.get(order.getDeliveryPersonId()).getName() : null);
            dto.setDeliveryRouteName(order.getDeliveryRouteId() != null && routeMap.containsKey(order.getDeliveryRouteId())
                    ? routeMap.get(order.getDeliveryRouteId()).getName() : null);
            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 根据用户名获取收货地址列表（用于分配配送时选择）
     */
    @GetMapping("/addresses")
    public ResponseEntity<List<UserAddress>> listAddresses(@RequestParam("username") String username) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<UserAddress> list = addressMapper.selectList(
                new QueryWrapper<UserAddress>().eq("username", username.trim()));
        return ResponseEntity.ok(list);
    }

    /**
     * 配送员列表
     */
    @GetMapping("/persons")
    public ResponseEntity<List<DeliveryPerson>> listPersons() {
        return ResponseEntity.ok(deliveryPersonMapper.selectList(null));
    }

    /**
     * 配送路线列表
     */
    @GetMapping("/routes")
    public ResponseEntity<List<DeliveryRoute>> listRoutes() {
        return ResponseEntity.ok(deliveryRouteMapper.selectList(null));
    }

    /**
     * 新增配送员
     */
    @PostMapping("/persons")
    public ResponseEntity<?> createPerson(@RequestBody DeliveryPerson person) {
        if (person.getName() == null || person.getName().isBlank()
                || person.getPhone() == null || person.getPhone().isBlank()) {
            return ResponseEntity.badRequest().body("姓名和手机号不能为空");
        }
        person.setId(null);
        person.setStatus(person.getStatus() != null ? person.getStatus() : "ACTIVE");
        deliveryPersonMapper.insert(person);
        return ResponseEntity.ok(person);
    }

    /**
     * 新增配送路线
     */
    @PostMapping("/routes")
    public ResponseEntity<?> createRoute(@RequestBody DeliveryRoute route) {
        if (route.getName() == null || route.getName().isBlank()) {
            return ResponseEntity.badRequest().body("路线名称不能为空");
        }
        route.setId(null);
        deliveryRouteMapper.insert(route);
        return ResponseEntity.ok(route);
    }

    /**
     * 分配配送员和路线
     */
    @PutMapping("/orders/{id}/assign")
    public ResponseEntity<?> assign(@PathVariable Long id,
                                    @RequestParam(required = false) Long personId,
                                    @RequestParam(required = false) Long routeId,
                                    @RequestParam(required = false) Long addressId) {
        Order order = orderMapper.selectById(id);
        if (order == null) return ResponseEntity.notFound().build();
        if (personId != null) order.setDeliveryPersonId(personId);
        if (routeId != null) order.setDeliveryRouteId(routeId);
        if (addressId != null) order.setAddressId(addressId);
        if (order.getDeliveryStatus() == null || order.getDeliveryStatus().isBlank()) {
            order.setDeliveryStatus("PENDING_ACCEPT");
        }
        orderMapper.updateById(order);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新配送状态：PENDING_ACCEPT | IN_DELIVERY | DELIVERED | EXCEPTION
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestParam String status,
                                          @RequestParam(required = false) String remark) {
        if (status == null || !List.of("PENDING_ACCEPT", "IN_DELIVERY", "DELIVERED", "EXCEPTION").contains(status)) {
            return ResponseEntity.badRequest().body("状态不合法");
        }
        Order order = orderMapper.selectById(id);
        if (order == null) return ResponseEntity.notFound().build();
        order.setDeliveryStatus(status);
        if (remark != null) order.setDeliveryRemark(remark);
        orderMapper.updateById(order);
        return ResponseEntity.ok().build();
    }
}
