package com.itcjj.campusmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcjj.campusmart.entity.Category;
import com.itcjj.campusmart.mapper.CategoryMapper;
import com.itcjj.campusmart.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        // 查询列表不打日志（会被翻页刷爆）
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
    }

}
