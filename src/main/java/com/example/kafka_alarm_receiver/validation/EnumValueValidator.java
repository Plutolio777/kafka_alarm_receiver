package com.example.kafka_alarm_receiver.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * EnumValueValidator 是用于处理自定义注解 [@EnumValue]的验证器。
 * 该验证器用于校验 CharSequence 类型（如 String）的值是否属于指定枚举类中的合法枚举名称。
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {

    // 存储目标枚举类的所有枚举值
    private Enum<?>[] enumValues;

    /**
     * 初始化方法，从注解中获取目标枚举类型，并提取所有枚举常量。
     *
     * @param constraintAnnotation 注解实例，包含配置信息
     */
    @Override
    public void initialize(EnumValue constraintAnnotation) {
        enumValues = constraintAnnotation.enumClass().getEnumConstants();
    }

    /**
     * 验证传入的 CharSequence 值是否是枚举类中定义的有效名称。
     *
     * @param value 待验证的值
     * @param context 校验上下文
     * @return 如果值有效或为空则返回 true，否则返回 false
     */
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null || value.toString().isEmpty()) {
            return true;  // 允许空值（可根据实际业务需求调整）
        }

        // 检查值是否匹配任意一个枚举名称
        for (Enum<?> enumValue : enumValues) {
            if (enumValue.name().equals(value.toString())) {
                return true;
            }
        }

        // 如果未找到匹配项，返回 false 表示校验失败
        return false;
    }
}
