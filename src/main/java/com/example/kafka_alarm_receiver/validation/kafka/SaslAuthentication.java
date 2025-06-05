package com.example.kafka_alarm_receiver.validation.kafka;

/**
 * SaslAuthentication 枚举类用于表示 Kafka 支持的 SASL 认证机制。
 * 提供根据认证名称获取枚举值的功能。
 */
public enum SaslAuthentication {

    /**
     * PLAIN 认证机制
     */
    PLAIN("PLAIN"),

    /**
     * SCRAM 认证机制
     */
    SCRAM("SCRAM");

    // 与枚举值对应的认证名称
    private final String roleName;

    /**
     * 构造方法，绑定枚举值与认证名称
     *
     * @param roleName 认证机制名称
     */
    SaslAuthentication(String roleName) {
        this.roleName = roleName;
    }

    /**
     * 获取该枚举值对应的认证机制名称
     *
     * @return 返回认证机制名称
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * 根据认证机制名称获取对应的枚举值
     *
     * @param roleName 认证机制名称
     * @return 返回匹配的 SaslAuthentication 枚举
     * @throws IllegalArgumentException 如果没有找到匹配的枚举值
     */
    public static SaslAuthentication fromRoleName(String roleName) {
        for (SaslAuthentication role : SaslAuthentication.values()) {
            if (role.getRoleName().equals(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + roleName);
    }
}
