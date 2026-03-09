package org.example.supermarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.supermarket.entity.DeliveryPerson;

@Mapper
public interface DeliveryPersonMapper extends BaseMapper<DeliveryPerson> {
}
