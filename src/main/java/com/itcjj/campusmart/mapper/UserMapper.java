package com.itcjj.campusmart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itcjj.campusmart.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
