package org.example.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.DeliveryPersonMapper;
import org.example.supermarket.dao.DeliveryRouteMapper;
import org.example.supermarket.dao.OrderItemMapper;
import org.example.supermarket.dao.OrderMapper;
import org.example.supermarket.dao.UserAddressMapper;
import org.example.supermarket.dto.DeliveryOrderDto;
import org.example.supermarket.entity.DeliveryPerson;
import org.example.supermarket.entity.DeliveryRoute;
import org.example.supermarket.entity.Order;
import org.example.supermarket.entity.OrderItem;
import org.example.supermarket.entity.UserAddress;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserAddressMapper addressMapper;
    private final DeliveryPersonMapper deliveryPersonMapper;
    private final DeliveryRouteMapper deliveryRouteMapper;

    public DeliveryService(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                          UserAddressMapper addressMapper, DeliveryPersonMapper deliveryPersonMapper,
                          DeliveryRouteMapper deliveryRouteMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.addressMapper = addressMapper;
        this.deliveryPersonMapper = deliveryPersonMapper;
        this.deliveryRouteMapper = deliveryRouteMapper;
    }

    public List<DeliveryOrderDto> listOrders() {
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().orderByDesc("created_at", "id"));
        if (orders.isEmpty()) {
            return List.of();
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
        return result;
    }

    public List<UserAddress> listAddresses(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        return addressMapper.selectList(
                new QueryWrapper<UserAddress>().eq("username", username.trim()));
    }

    public List<DeliveryPerson> listPersons() {
        return deliveryPersonMapper.selectList(null);
    }

    public List<DeliveryRoute> listRoutes() {
        return deliveryRouteMapper.selectList(null);
    }

    public DeliveryPerson createPerson(DeliveryPerson person) {
        if (person.getName() == null || person.getName().isBlank()
                || person.getPhone() == null || person.getPhone().isBlank()) {
            throw new IllegalArgumentException("姓名和手机号不能为空");
        }
        person.setId(null);
        person.setStatus(person.getStatus() != null ? person.getStatus() : "ACTIVE");
        deliveryPersonMapper.insert(person);
        return person;
    }

    public DeliveryRoute createRoute(DeliveryRoute route) {
        if (route.getName() == null || route.getName().isBlank()) {
            throw new IllegalArgumentException("路线名称不能为空");
        }
        route.setId(null);
        deliveryRouteMapper.insert(route);
        return route;
    }

    public boolean assignOrder(Long orderId, Long personId, Long routeId, Long addressId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) return false;
        if (personId != null) order.setDeliveryPersonId(personId);
        if (routeId != null) order.setDeliveryRouteId(routeId);
        if (addressId != null) order.setAddressId(addressId);
        if (order.getDeliveryStatus() == null || order.getDeliveryStatus().isBlank()) {
            order.setDeliveryStatus("PENDING_ACCEPT");
        }
        orderMapper.updateById(order);
        return true;
    }

    public boolean updateOrderStatus(Long orderId, String status, String remark) {
        if (status == null || !List.of("PENDING_ACCEPT", "IN_DELIVERY", "DELIVERED", "EXCEPTION").contains(status)) {
            throw new IllegalArgumentException("状态不合法");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) return false;
        order.setDeliveryStatus(status);
        if (remark != null) order.setDeliveryRemark(remark);
        orderMapper.updateById(order);
        return true;
    }
}
