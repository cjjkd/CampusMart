package com.itcjj.campusmart.service.impl;

import com.itcjj.campusmart.dto.UserDTO;
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
    @Override
    public void add(UserDTO dto){
        User user=new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setCampus(dto.getCampus());
        userMapper.insert(user);
    }
    @Override
    public void update(UserDTO dto){
        User user=new User();
        user.setId(dto.getId());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setCampus(dto.getCampus());
        userMapper.updateById(user);
    }
    @Override
    public void delete(Long id){
        userMapper.deleteById(id);
    }
}
