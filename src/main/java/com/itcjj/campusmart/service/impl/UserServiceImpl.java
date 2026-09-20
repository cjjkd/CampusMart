package com.itcjj.campusmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.common.CurrentUser;
import com.itcjj.campusmart.dto.LoginDTO;
import com.itcjj.campusmart.dto.PasswordDTO;
import com.itcjj.campusmart.dto.UserDTO;
import com.itcjj.campusmart.entity.User;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.mapper.UserMapper;
import com.itcjj.campusmart.service.UserService;
import com.itcjj.campusmart.util.JwtUtil;
import com.itcjj.campusmart.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setCampus(dto.getCampus());
        userMapper.insert(user);

        // insert 后 id 已被 MP 回填，可以拿到。只记 id 和 username，绝不记密码
        log.info("用户注册成功 -> id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public void update(UserDTO dto){
        // ★ 越权校验：既不是自己，又不是管理员 → 拒
        Long currentId = UserContext.get().getId();
        String role = UserContext.get().getRole();
        if (!currentId.equals(dto.getId()) && !"ADMIN".equals(role)) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        User user=new User();
        user.setId(dto.getId());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setCampus(dto.getCampus());
        userMapper.updateById(user);
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BizException(CodeEnum.USER_NOT_FOUND);
        }


        // 改别人资料是敏感操作：记下「谁改的、改的是谁」
        log.info("用户资料更新 -> 操作人={}, 目标用户={}", currentId, dto.getId());
    }

    @Override
    public void delete(Long id){
        userMapper.deleteById(id);
        int rows = userMapper.deleteById(id);
        if (rows == 0) {
            throw new BizException(CodeEnum.USER_NOT_FOUND);
        }

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
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(CodeEnum.LOGIN_FAILED);
        }


        // 登录成功记一笔（安全审计：谁、什么时候登录过）
        log.info("用户登录成功 -> id={}, username={}", user.getId(), user.getUsername());

        //第四步：都过了，生成并且返回token
        return jwtUtil.createToken(user.getId(), user.getRole(), user.getTokenVersion());
    }

    @Override
    public User getCurrentUser() {
        CurrentUser current = UserContext.get();// 👈 关键在这行
        return userMapper.selectById(current.getId());
    }

    @Override
    public void updatePassword(PasswordDTO dto) {
        Long userId = UserContext.get().getId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(CodeEnum.USER_NOT_FOUND);
        }

        // 1. 验原密码：证明「你是本人」，不只是「你手上有 token」
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException(CodeEnum.OLD_PASSWORD_WRONG);
        }

        // 2. 一次 UPDATE 同时干两件事：换新密码（自动生成新盐）+ 版本号 +1
        User upd = new User();
        upd.setId(userId);
        upd.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        upd.setTokenVersion(user.getTokenVersion() + 1);
        userMapper.updateById(upd);

        // 只记 id：新旧密码都不能进日志
        log.info("用户修改密码 -> id={}，该用户所有旧 token 已作废", userId);
    }
    @Value("${campusmart.upload.path}")
    private String uploadPath;

    /** 允许的图片扩展名 —— 白名单，不是黑名单 */
    private static final List<String> ALLOWED_EXT = List.of("jpg", "jpeg", "png", "gif", "webp");

    @Override
    public String uploadAvatar(MultipartFile file) {
        // 1. 空文件
        if (file == null || file.isEmpty()) {
            throw new BizException(CodeEnum.FILE_EMPTY);
        }

        // 2. 取扩展名 + 白名单校验（绝不使用用户传的完整文件名）
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.lastIndexOf('.') >= 0) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BizException(CodeEnum.FILE_TYPE_NOT_ALLOWED);
        }
        // 2.5 魔数校验：光看扩展名不够，内容必须真的是图片
        try {
            if (!isRealImage(file, ext)) {
                throw new BizException(CodeEnum.FILE_TYPE_NOT_ALLOWED);
            }
        } catch (IOException e) {
            throw new BizException(CodeEnum.FILE_UPLOAD_FAILED);
        }


        // 3. 自己生成文件名，杜绝路径穿越和重名覆盖
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        // 4. 落盘（目录不存在就自动创建）
        File dir = new File(uploadPath + "avatar/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("头像落盘失败 -> id={}", UserContext.get().getId(), e);
            throw new BizException(CodeEnum.FILE_UPLOAD_FAILED);
        }

        // 5. 库里只存访问路径（不存二进制）
        String url = "/upload/avatar/" + fileName;
        Long userId = UserContext.get().getId();
        User upd = new User();
        upd.setId(userId);
        upd.setAvatar(url);
        userMapper.updateById(upd);

        log.info("用户上传头像 -> id={}, url={}", userId, url);
        return url;
    }
    /** 读文件头几个字节，判断真实类型（魔数校验）—— 光看扩展名挡不住伪造 */
    private boolean isRealImage(MultipartFile file, String ext) throws IOException {
        byte[] head = new byte[8];
        try (InputStream in = file.getInputStream()) {
            if (in.read(head) < 4) {
                return false;                       // 文件太小，不可能有文件头，直接否
            }
        }
        if ("png".equals(ext)) {
            return (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G';
        }
        if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            return (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
        }
        if ("gif".equals(ext)) {
            return head[0] == 'G' && head[1] == 'I' && head[2] == 'F';
        }
        if ("webp".equals(ext)) {
            return head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F';
        }
        return false;
    }




}
