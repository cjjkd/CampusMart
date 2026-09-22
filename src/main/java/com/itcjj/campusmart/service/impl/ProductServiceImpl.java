package com.itcjj.campusmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcjj.campusmart.common.CodeEnum;
import com.itcjj.campusmart.dto.ProductDTO;
import com.itcjj.campusmart.dto.ProductUpdateDTO;
import com.itcjj.campusmart.entity.Product;
import com.itcjj.campusmart.exception.BizException;
import com.itcjj.campusmart.mapper.ProductMapper;
import com.itcjj.campusmart.service.ProductService;
import com.itcjj.campusmart.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Long publish(ProductDTO dto) {
        // 1. DTO（入参袋子）→ Entity（表里的一行）
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setConditionLevel(dto.getConditionLevel());
        product.setCategoryId(dto.getCategoryId());

        // 2. ★★★ 卖家从 token 取，不信前端 —— 今天整节课就为这一行
        product.setSellerId(UserContext.get().getId());

        // 3. 刚发布就是在售，不让用户决定
        product.setStatus(1);

        // 4. 落库
        productMapper.insert(product);


        // 5. insert 之后 MP 会把自增 id 回填进 product 对象，这时才有值
        log.info("商品发布成功 -> sellerId={}, productId={}, title={}",
                product.getSellerId(), product.getId(), product.getTitle());
        return product.getId();
    }
    @Override
    public void update(ProductUpdateDTO dto) {
        Product old = productMapper.selectById(dto.getId());
        if (old == null) {
            throw new BizException(CodeEnum.PRODUCT_NOT_FOUND);
        }
        if (!old.getSellerId().equals(UserContext.get().getId())) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        Product product = new Product();
        product.setId(dto.getId());
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setConditionLevel(dto.getConditionLevel());
        product.setCategoryId(dto.getCategoryId());
        int rows=productMapper.updateById(product);
        if (rows == 0) {
            throw new BizException(CodeEnum.PRODUCT_NOT_FOUND);
        }
        log.info("商品更新成功 -> 操作人={}, productId={}", UserContext.get().getId(), product.getId());

    }
    @Override
    public void offline(Long id) {
        Product old = productMapper.selectById(id);
        if (old == null) {
            throw new BizException(CodeEnum.PRODUCT_NOT_FOUND);
        }
        if (!old.getSellerId().equals(UserContext.get().getId())) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        Product product = new Product();
        product.setId(id);
        product.setStatus(0);
        int rows=productMapper.updateById(product);
        if (rows == 0) {
            throw new BizException(CodeEnum.PRODUCT_NOT_FOUND);
        }
        log.info("商品下架成功 -> 操作人={}, productId={}", UserContext.get().getId(), product.getId());
    }
    @Override
    public void delete(Long id) {
        Product old = productMapper.selectById(id);
        if (old == null) {
            throw new BizException(CodeEnum.PRODUCT_NOT_FOUND);
        }
        if (!old.getSellerId().equals(UserContext.get().getId())) {
            throw new BizException(CodeEnum.NO_PERMISSION);
        }
        int rows=productMapper.deleteById(id);
        if (rows == 0) {
            throw new BizException(CodeEnum.PRODUCT_NOT_FOUND);
        }
        log.info("商品删除成功 -> 操作人={}, productId={}", UserContext.get().getId(), id);
    }
    @Override// ProductServiceImpl
    public List<Product> listByCategory(Long categoryId) {
        return productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, categoryId)
                        .eq(Product::getStatus, 1)
                        .orderByDesc(Product::getCreateTime));
    }


}
