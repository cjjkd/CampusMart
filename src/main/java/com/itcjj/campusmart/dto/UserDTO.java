package com.itcjj.campusmart.dto;


import com.itcjj.campusmart.annotation.Phone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message="用户名不能为空", groups = {ValidGroup.Create.class})
    @Size(min=3, max=20, message="用户名长度在3到20个字符之间", groups = {ValidGroup.Create.class})
    private String username;
    @NotBlank(message="密码不能为空", groups = {ValidGroup.Create.class})
    @Size(min=6, max=20, message="密码长度在6到20个字符之间", groups = {ValidGroup.Create.class})
    private String password;
    @Size(max=20, message="昵称长度在20个字符以内", groups = {ValidGroup.Create.class, ValidGroup.Update.class})
    private String nickname;
    @Phone(message = "手机号格式不正确", groups = {ValidGroup.Create.class, ValidGroup.Update.class})
    private String phone;
    private String campus;

}
