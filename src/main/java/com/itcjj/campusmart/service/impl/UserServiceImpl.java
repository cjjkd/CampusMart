package com.itcjj.campusmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.dto.LoginDTO;
import com.itcjj.campusmart.dto.UserDTO;
import com.itcjj.campusmart.entity.User;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.mapper.UserMapper;
import com.itcjj.campusmart.service.UserService;
import com.itcjj.campusmart.util.JwtUtil;
import com.itcjj.campusmart.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public List<User> listAll() {
        return userMapper.selectList(null);
    }
    @Override
    public void add(UserDTO dto){
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BizException(CodeEnum.USERNAME_EXIST);
        }

        // 设置用户信息
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

    @Override
    public String login(LoginDTO dto) {
        // 实现登录逻辑
        //第一步，按用户名查出这个用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        //第二步，没这个人，抛异常
        //第三步，密码错误，抛异常
        if (user == null || !user.getPassword().equals(dto.getPassword())) {
            throw new BizException(CodeEnum.LOGIN_FAILED);
        }

        //第四步：都过了，生成并且返回token
        return jwtUtil.createToken(user.getId());
    }
    @Override
    public User getCurrentUser() {
        Long userId = UserContext.get();      // 👈 关键在这行
        return userMapper.selectById(userId);
    }

}
