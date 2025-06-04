package com.example.kafka_alarm_receiver.validation.kafka;

public enum SaslAuthentication {
    PLAIN("PLAIN"),
    SCRAM("SCRAM");


    private final String roleName;

    SaslAuthentication(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static SaslAuthentication fromRoleName(String roleName) {
        for (SaslAuthentication role : SaslAuthentication.values()) {
            if (role.getRoleName().equals(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + roleName);
    }
}
