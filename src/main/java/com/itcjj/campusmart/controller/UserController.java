package com.itcjj.campusmart.controller;

import com.itcjj.campusmart.common.Result;
import com.itcjj.campusmart.dto.UserDTO;
import com.itcjj.campusmart.entity.User;
import com.itcjj.campusmart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 获取用户列表
    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userService.listAll());
    }
    // 添加用户
    @PostMapping("/add")
    public Result<Void> add(@RequestBody UserDTO dto) {
        userService.add(dto);
        return Result.success(null);
    }

    // 更新用户
    @PutMapping("/update")
    public Result<Void> update(@RequestBody UserDTO dto) {
        userService.update(dto);
        return Result.success(null);
    }

    // 删除用户
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success(null);
    }

}
