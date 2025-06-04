package com.example.kafka_alarm_receiver.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {

    private Enum<?>[] enumValues;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        // 获取枚举类并将其所有值赋给 enumValues
        enumValues = constraintAnnotation.enumClass().getEnumConstants();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null || value.toString().isEmpty()) {
            return true;  // 允许空值（可以根据实际需求调整）
        }

        // 遍历所有枚举值，检查该值是否存在于枚举中
        for (Enum<?> enumValue : enumValues) {
            if (enumValue.name().equals(value.toString())) {
                return true;
            }
        }
        return false; // 如果没有找到匹配的枚举值，则返回 false
    }
}
