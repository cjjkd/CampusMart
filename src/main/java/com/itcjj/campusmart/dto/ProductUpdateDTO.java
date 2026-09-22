package com.itcjj.campusmart.dto;
import java.math.BigDecimal;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductUpdateDTO {
    @NotNull(message = "ID 不能为空")
    private Long id;
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过 100 个字符")
    private String title;
    private String description;
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;
    @NotNull(message = "成色不能为空")
    @Max(value = 4, message = "成色必须在 1 到 4 之间")
    @Min(value = 1, message = "成色必须在 1 到 4 之间")
    private Integer conditionLevel;
    @NotNull(message = "分类 ID 不能为空")
    private Long categoryId;
}
