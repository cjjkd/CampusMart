package com.itcjj.campusmart.service;

import com.itcjj.campusmart.dto.ProductDTO;
import com.itcjj.campusmart.dto.ProductUpdateDTO;
import com.itcjj.campusmart.entity.Product;

import java.util.List;

public interface ProductService {
    Long publish(ProductDTO dto);
    void update(ProductUpdateDTO dto);

    void offline(Long id);

    void delete(Long id);

    List<Product> listByCategory(Long categoryId);
}

