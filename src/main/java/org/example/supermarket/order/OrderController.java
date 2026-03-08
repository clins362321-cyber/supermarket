package org.example.supermarket.order;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.order.dto.OrderCreateDto;
import org.example.supermarket.order.dto.OrderItemDto;
import org.example.supermarket.order.dto.OrderWithItemsDto;
import org.example.supermarket.order.mapper.OrderItemMapper;
import org.example.supermarket.order.mapper.OrderMapper;
import org.example.supermarket.product.Product;
import org.example.supermarket.product.mapper.ProductMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    public OrderController(OrderMapper orderMapper,
                           OrderItemMapper orderItemMapper,
                           ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
    }

    /**
     * 创建订单（根据当前购物车）
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody OrderCreateDto dto) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("订单商品为空");
        }
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }

        List<OrderItemDto> items = dto.getItems();

        // 校验数量
        for (OrderItemDto item : items) {
            if (item.getProductId() == null || item.getQty() == null || item.getQty() <= 0) {
                return ResponseEntity.badRequest().body("商品数量不合法");
            }
        }

        // 查询商品信息
        List<Long> productIds = items.stream()
                .map(OrderItemDto::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<Product> products = productMapper.selectBatchIds(productIds);
        if (products.size() != productIds.size()) {
            return ResponseEntity.badRequest().body("存在不存在的商品，无法下单");
        }

        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : products) {
            productMap.put(p.getId(), p);
        }

        // 校验库存并计算总价
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDto item : items) {
            Product p = productMap.get(item.getProductId());
            if (p.getStock() == null || p.getStock() < item.getQty()) {
                return ResponseEntity.badRequest().body("商品库存不足：" + p.getName());
            }
            BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
            total = total.add(sub);
        }

        // 创建订单
        Order order = new Order();
        order.setUsername(dto.getUsername().trim());
        order.setTotalPrice(total);
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        // 创建订单明细并扣减库存
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

        return ResponseEntity.ok(order.getId());
    }

    /**
     * 根据用户名查询该用户的所有订单及明细（用于“售后”页面展示）
     */
    @GetMapping("/by-user")
    public ResponseEntity<?> listByUser(@RequestParam("username") String username) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>()
                        .eq("username", username.trim())
                        .orderByDesc("created_at", "id")
        );
        if (orders.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
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
        return ResponseEntity.ok(result);
    }
}

