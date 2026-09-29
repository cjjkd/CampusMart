package com.itcjj.campusmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSearchDTO {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;	// 最小价格
    private BigDecimal maxPrice;	// 最大价格
    private Integer conditionLevel;
    private Integer pageNum=1;	// 默认 1
    private Integer pageSize=10;	// 默认 10
    private String sortBy;
}
