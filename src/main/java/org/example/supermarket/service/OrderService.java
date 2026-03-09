package org.example.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.OrderItemMapper;
import org.example.supermarket.dao.OrderMapper;
import org.example.supermarket.dao.ProductMapper;
import org.example.supermarket.dto.OrderCreateDto;
import org.example.supermarket.dto.OrderItemDto;
import org.example.supermarket.dto.OrderWithItemsDto;
import org.example.supermarket.entity.Order;
import org.example.supermarket.entity.OrderItem;
import org.example.supermarket.entity.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    @Transactional
    public Long createOrder(OrderCreateDto dto) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单商品为空");
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        List<OrderItemDto> items = dto.getItems();

        for (OrderItemDto item : items) {
            if (item.getProductId() == null || item.getQty() == null || item.getQty() <= 0) {
                throw new IllegalArgumentException("商品数量不合法");
            }
        }

        List<Long> productIds = items.stream()
                .map(OrderItemDto::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<Product> products = productMapper.selectBatchIds(productIds);
        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("存在不存在的商品，无法下单");
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : products) {
            productMap.put(p.getId(), p);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDto item : items) {
            Product p = productMap.get(item.getProductId());
            if (p.getStock() == null || p.getStock() < item.getQty()) {
                throw new IllegalArgumentException("商品库存不足：" + p.getName());
            }
            BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
            total = total.add(sub);
        }

        Order order = new Order();
        order.setUsername(dto.getUsername().trim());
        order.setTotalPrice(total);
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryStatus("PENDING_ACCEPT");
        orderMapper.insert(order);

        for (OrderItemDto item : items) {
            Product p = productMap.get(item.getProductId());
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setPrice(p.getPrice());
            oi.setQty(item.getQty());
            orderItemMapper.insert(oi);

            int nextStock = Objects.requireNonNullElse(p.getStock(), 0) - item.getQty();
            p.setStock(Math.max(0, nextStock));
            productMapper.updateById(p);
        }

        return order.getId();
    }

    public List<OrderWithItemsDto> listByUser(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>()
                        .eq("username", username.trim())
                        .orderByDesc("created_at", "id")
        );
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().in("order_id", orderIds)
        );

        Map<Long, List<OrderItem>> map = new HashMap<>();
        for (OrderItem item : items) {
            map.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
        }

        List<OrderWithItemsDto> result = new ArrayList<>();
        for (Order order : orders) {
            OrderWithItemsDto dto = new OrderWithItemsDto();
            dto.setOrder(order);
            dto.setItems(map.getOrDefault(order.getId(), Collections.emptyList()));
            result.add(dto);
        }
        return result;
    }
}
