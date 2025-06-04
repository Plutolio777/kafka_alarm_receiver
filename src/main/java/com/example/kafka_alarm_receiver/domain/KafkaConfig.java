package com.example.kafka_alarm_receiver.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName kafka_config
 */
@TableName(value ="kafka_config")
@Data
public class KafkaConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String address;
    private String topic;
    private String consumerGroup;
    private String authentication;
    private String username;
    private String password;
    private Long createUserId;
    private Long updateUserId;
    private Integer dataResource;
    private Integer connectionStatus;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        KafkaConfig other = (KafkaConfig) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getAddress() == null ? other.getAddress() == null : this.getAddress().equals(other.getAddress()))
            && (this.getTopic() == null ? other.getTopic() == null : this.getTopic().equals(other.getTopic()))
            && (this.getConsumerGroup() == null ? other.getConsumerGroup() == null : this.getConsumerGroup().equals(other.getConsumerGroup()))
            && (this.getAuthentication() == null ? other.getAuthentication() == null : this.getAuthentication().equals(other.getAuthentication()))
            && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getCreateUserId() == null ? other.getCreateUserId() == null : this.getCreateUserId().equals(other.getCreateUserId()))
            && (this.getUpdateUserId() == null ? other.getUpdateUserId() == null : this.getUpdateUserId().equals(other.getUpdateUserId()))
            && (this.getDataResource() == null ? other.getDataResource() == null : this.getDataResource().equals(other.getDataResource()))
            && (this.getConnectionStatus() == null ? other.getConnectionStatus() == null : this.getConnectionStatus().equals(other.getConnectionStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
        result = prime * result + ((getTopic() == null) ? 0 : getTopic().hashCode());
        result = prime * result + ((getConsumerGroup() == null) ? 0 : getConsumerGroup().hashCode());
        result = prime * result + ((getAuthentication() == null) ? 0 : getAuthentication().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getCreateUserId() == null) ? 0 : getCreateUserId().hashCode());
        result = prime * result + ((getUpdateUserId() == null) ? 0 : getUpdateUserId().hashCode());
        result = prime * result + ((getDataResource() == null) ? 0 : getDataResource().hashCode());
        result = prime * result + ((getConnectionStatus() == null) ? 0 : getConnectionStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", name=" + name +
                ", address=" + address +
                ", topic=" + topic +
                ", consumerGroup=" + consumerGroup +
                ", authentication=" + authentication +
                ", username=" + username +
                ", password=" + password +
                ", createUserId=" + createUserId +
                ", updateUserId=" + updateUserId +
                ", dataResource=" + dataResource +
                ", connectionStatus=" + connectionStatus +
                ", serialVersionUID=" + serialVersionUID +
                "]";
    }
}