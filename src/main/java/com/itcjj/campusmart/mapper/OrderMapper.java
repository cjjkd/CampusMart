package com.itcjj.campusmart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itcjj.campusmart.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
