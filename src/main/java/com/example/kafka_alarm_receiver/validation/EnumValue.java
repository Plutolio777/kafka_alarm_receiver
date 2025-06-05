package com.example.kafka_alarm_receiver.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnumValue 是一个自定义注解，用于验证字段或参数的值是否为指定枚举类中的合法值。
 *
 * 使用此注解可以确保被注解的字段、方法或参数的输入值属于特定的枚举类型。
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValueValidator.class) // 指定对应的验证器实现类
public @interface EnumValue {

    /**
     * 验证失败时返回的默认错误消息。
     *
     * @return 错误消息字符串
     */
    String message() default "Invalid enum value";  // 默认错误消息

    /**
     * 定义校验分组，可指定不同场景下是否进行验证。
     *
     * @return 分组类数组
     */
    Class<?>[] groups() default {};                // 校验分组（可选）

    /**
     * 可以携带额外信息，例如日志级别、安全策略等。
     *
     * @return Payload 数组
     */
    Class<? extends Payload>[] payload() default {};  // 负载信息（如日志等级）

    /**
     * 需要校验的目标枚举类型，必须继承自 Enum。
     *
     * @return 枚举类的 Class 对象
     */
    Class<? extends Enum<?>> enumClass();  // 枚举类型
}
