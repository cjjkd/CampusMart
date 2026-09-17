package com.itcjj.campusmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.common.CurrentUser;
import com.itcjj.campusmart.dto.LoginDTO;
import com.itcjj.campusmart.dto.UserDTO;
import com.itcjj.campusmart.entity.User;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.mapper.UserMapper;
import com.itcjj.campusmart.service.UserService;
import com.itcjj.campusmart.util.JwtUtil;
import com.itcjj.campusmart.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public List<User> listAll() {
        // 查询列表不打日志：会被翻页刷爆，没有留存价值
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

        // insert 后 id 已被 MP 回填，可以拿到。只记 id 和 username，绝不记密码
        log.info("用户注册成功 -> id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public void update(UserDTO dto){
        User user=new User();
        user.setId(dto.getId());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setCampus(dto.getCampus());
        userMapper.updateById(user);

        // 只记 id：phone 属于个人信息，不进日志
        log.info("用户资料更新 -> id={}", dto.getId());
    }

    @Override
    public void delete(Long id){
        userMapper.deleteById(id);

        // 删除是敏感操作：记下「谁删了谁」，出问题能追溯
        log.info("删除用户 -> 操作人={}, 目标用户={}", UserContext.get().getId(), id);
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

        // 登录成功记一笔（安全审计：谁、什么时候登录过）
        log.info("用户登录成功 -> id={}, username={}", user.getId(), user.getUsername());

        //第四步：都过了，生成并且返回token
        return jwtUtil.createToken(user.getId(),user.getRole());
    }

    @Override
    public User getCurrentUser() {
        CurrentUser current = UserContext.get();// 👈 关键在这行
        return userMapper.selectById(current.getId());
    }

}
