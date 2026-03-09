package org.example.supermarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.supermarket.entity.Supplier;

@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {
}
