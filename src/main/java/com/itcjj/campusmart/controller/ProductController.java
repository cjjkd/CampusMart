package com.itcjj.campusmart.controller;

import com.itcjj.campusmart.common.Result;
import com.itcjj.campusmart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import com.itcjj.campusmart.dto.ProductDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @PostMapping("/publish")
    public Result<Void> publish(@Valid@RequestBody ProductDTO productDTO) {
        productService.publish(productDTO);
        return Result.success(null);
    }
}
