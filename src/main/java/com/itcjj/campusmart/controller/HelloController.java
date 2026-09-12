package com.itcjj.campusmart.controller;

import com.itcjj.campusmart.common.Result;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("hello world");
    }
}

