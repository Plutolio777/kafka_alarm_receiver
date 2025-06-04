package com.example.kafka_alarm_receiver.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValueValidator.class) // 指定验证器
public @interface EnumValue {
    String message() default "Invalid enum value";  // 默认错误消息
    Class<?>[] groups() default {};                // 组
    Class<? extends Payload>[] payload() default {};  // 负载
    Class<? extends Enum<?>> enumClass();  // 枚举类型
}
