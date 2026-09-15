package com.itcjj.campusmart.service;

import com.itcjj.campusmart.dto.LoginDTO;
import com.itcjj.campusmart.entity.User;
import java.util.List;
import com.itcjj.campusmart.dto.UserDTO;


public interface UserService{
    List<User> listAll();
    void add (UserDTO dto);
    void update(UserDTO dto);
    void delete(Long id);
    String login (LoginDTO dto);
}
