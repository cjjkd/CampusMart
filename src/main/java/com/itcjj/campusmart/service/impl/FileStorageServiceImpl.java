package com.itcjj.campusmart.service.impl;

import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    @Value("${campusmart.upload.path}")
    private String uploadPath;
    /**
     * 允许的图片扩展名 —— 白名单，不是黑名单
     */
    private static final List<String> ALLOWED_EXT = List.of("jpg", "jpeg", "png", "gif", "webp");

    @Override
    public String save(MultipartFile file, String subDir) {
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
        File dir = new File(uploadPath + subDir + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BizException(CodeEnum.FILE_UPLOAD_FAILED);
        }
        return "/upload/" + subDir + "/" + fileName;
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
