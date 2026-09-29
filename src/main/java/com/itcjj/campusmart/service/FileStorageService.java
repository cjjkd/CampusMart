package com.itcjj.campusmart.service;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Service;

public interface FileStorageService {
    String save(MultipartFile file,String subDir);
}
