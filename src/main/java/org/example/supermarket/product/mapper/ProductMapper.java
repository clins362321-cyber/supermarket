package org.example.supermarket.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.supermarket.product.Product;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}

