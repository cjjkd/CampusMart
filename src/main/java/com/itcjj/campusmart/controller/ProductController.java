package com.itcjj.campusmart.controller;

import com.itcjj.campusmart.common.Result;
import com.itcjj.campusmart.dto.ProductUpdateDTO;
import com.itcjj.campusmart.entity.Product;
import com.itcjj.campusmart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.itcjj.campusmart.dto.ProductDTO;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;
    @PostMapping("/publish")
    public Result<Long> publish(@Valid @RequestBody ProductDTO dto) {
        Long id = productService.publish(dto);
        return Result.success(id);
    }
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody ProductUpdateDTO dto) {
        productService.update(dto);
        return Result.success(null);
    }
    @PutMapping("/offline/{id}")
    public Result<Void> offline(@PathVariable Long id) {
        productService.offline(id);
        return Result.success(null);
    }
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success(null);
    }
    // ProductController
    @GetMapping("/list")
    public Result<List<Product>> list(@RequestParam Long categoryId) {
        return Result.success(productService.listByCategory(categoryId));
    }



}
