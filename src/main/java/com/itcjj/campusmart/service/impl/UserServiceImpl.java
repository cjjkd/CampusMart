package com.itcjj.campusmart.service.impl;

import com.itcjj.campusmart.entity.User;
import com.itcjj.campusmart.mapper.UserMapper;
import com.itcjj.campusmart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> listAll() {
        return userMapper.selectList(null);
    }
}
