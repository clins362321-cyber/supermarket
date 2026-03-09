package org.example.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.AfterSalesMapper;
import org.example.supermarket.dao.OrderItemMapper;
import org.example.supermarket.dao.OrderMapper;
import org.example.supermarket.dto.AfterSalesCreateDto;
import org.example.supermarket.entity.AfterSales;
import org.example.supermarket.entity.Order;
import org.example.supermarket.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AfterSalesService {

    private final AfterSalesMapper afterSalesMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public AfterSalesService(AfterSalesMapper afterSalesMapper, OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.afterSalesMapper = afterSalesMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public AfterSales create(AfterSalesCreateDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("请先登录");
        }
        if (dto.getOrderId() == null || dto.getOrderItemId() == null || dto.getProductId() == null) {
            throw new IllegalArgumentException("订单信息不完整");
        }

        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUsername().equals(dto.getUsername().trim())) {
            throw new IllegalArgumentException("订单不存在或无权操作");
        }

        OrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        if (item == null || !item.getOrderId().equals(dto.getOrderId()) || !item.getProductId().equals(dto.getProductId())) {
            throw new IllegalArgumentException("订单明细不存在");
        }

        long exist = afterSalesMapper.selectCount(
            new QueryWrapper<AfterSales>()
                .eq("order_item_id", dto.getOrderItemId())
                .in("status", "PENDING", "PROCESSING")
        );
        if (exist > 0) {
            throw new IllegalArgumentException("该商品已有进行中的售后申请");
        }

        AfterSales as = new AfterSales();
        as.setOrderId(dto.getOrderId());
        as.setOrderItemId(dto.getOrderItemId());
        as.setProductId(dto.getProductId());
        as.setProductName(dto.getProductName() != null ? dto.getProductName() : item.getProductName());
        as.setUsername(dto.getUsername().trim());
        as.setReason(dto.getReason());
        as.setStatus("PENDING");
        as.setCreatedAt(LocalDateTime.now());
        afterSalesMapper.insert(as);
        return as;
    }

    public List<AfterSales> listByUser(String username) {
        if (username == null || username.isBlank()) return List.of();
        return afterSalesMapper.selectList(
            new QueryWrapper<AfterSales>()
                .eq("username", username.trim())
                .orderByDesc("created_at", "id")
        );
    }
}
