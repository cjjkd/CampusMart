package com.itcjj.campusmart.validator;

import com.itcjj.campusmart.annotation.Phone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<Phone, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 空值放行：管「空不空」是 @NotBlank 的事，我这里只管格式
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches("^1[3-9]\\d{9}$");
    }
}
