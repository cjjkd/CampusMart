package com.itcjj.campusmart.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String campus;

}
