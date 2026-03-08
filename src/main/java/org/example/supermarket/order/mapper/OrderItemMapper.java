package org.example.supermarket.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.supermarket.order.OrderItem;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}

