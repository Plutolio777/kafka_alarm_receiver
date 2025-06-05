package com.example.kafka_alarm_receiver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "<kafka_alarm_log-{now/d}>")
public class AlarmMessage {

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ACKNOWLEDGEMENTTIMESTAMP")
    private String srcAcknowledgementtimestamp;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("SRC_ALARM_NTIME")
    private String srcAlarmNtime;

    @Field(type = FieldType.Integer)
    @JsonProperty("SRC_PERCEIVEDSEVERITY")
    private Integer srcPerceivedseverity;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_MANU_ALARMFLAG")
    private String srcManuAlarmflag;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("SRC_EVENTTIME")
    private String srcEventtime;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_LOCATIONINFO")
    private String srcLocationinfo;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ALARMCODE")
    private String srcAlarmcode;

    @Field(type = FieldType.Long)
    @JsonProperty("SRC_ALARMID")
    private Long srcAlarmid;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_NMS_TYPE")
    private String srcNmsType;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_SYNC_FLAG")
    private String srcSyncFlag;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ALARMSUBTYPE")
    private String srcAlarmsubtype;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ALARM_NID")
    private String srcAlarmNid;

    @Field(type = FieldType.Keyword)
    @JsonProperty("sCT")
    private String sct;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_ALARM_PROBLEM")
    private String srcAlarmProblem;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_SPECIALTY")
    private String srcSpecialty;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ID")
    private String srcId;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_SYSTEM_TYPE")
    private String srcSystemType;

    @Field(type = FieldType.Keyword)
    @JsonProperty("sPS")
    private String sps;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ALARM_NO")
    private String srcAlarmNo;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_REGION")
    private String srcRegion;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_SYSTEM_DN")
    private String srcSystemDn;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ACKSTATUS")
    private String srcAckstatus;

    @Field(type = FieldType.Boolean)
    @JsonProperty("SRC_IS_TEST")
    private Boolean srcIsTest;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_ALARMTITLE")
    private String srcAlarmtitle;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO9")
    private String srcInfo9;

    @Field(type = FieldType.Ip)
    @JsonProperty("SRC_IP")
    private String srcIp;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_EQUIPMENTNAME")
    private String srcEquipmentname;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("SRC_INERTTIME")
    private String srcInerttime;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_SOURCE")
    private String srcSource;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ALARMTYPE")
    private String srcAlarmtype;

    @Field(type = FieldType.Boolean)
    @JsonProperty("SRC_CLEARANCEREPORTFLAG")
    private Boolean srcClearancereportflag;

    @Field(type = FieldType.Ip)
    @JsonProperty("SRC_IPADDRESS")
    private String srcIpaddress;

    @Field(type = FieldType.Integer)
    @JsonProperty("SRC_STATE")
    private Integer srcState;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_NAME")
    private String srcName;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_vendor_ALARMID")
    private String srcVendorAlarmid;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO8")
    private String srcInfo8;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO7")
    private String srcInfo7;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO6")
    private String srcInfo6;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO5")
    private String srcInfo5;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_NECLASS")
    private String srcNeclass;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_CLEARANCETIMESTAMP")
    private String srcClearancetimestamp;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO4")
    private String srcInfo4;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO3")
    private String srcInfo3;

    @Field(type = FieldType.Text)
    @JsonProperty("sEN")
    private String sen;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO2")
    private String srcInfo2;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_INFO1")
    private String srcInfo1;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_RESTORETYPE")
    private String srcRestoretype;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ORG_CLR_OPTR")
    private String srcOrgClrOptr;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_APP_ID")
    private String srcAppId;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ALARMSID")
    private String srcAlarmsid;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_VENDOR")
    private String srcVendor;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_ALARM_NTYPE")
    private String srcAlarmNtype;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_NETTYPE")
    private String srcNettype;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("SRC_SENDTIME")
    private String srcSendtime;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_MANU_ALARMTYPE")
    private String srcManuAlarmtype;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_ADDITIONALTEXT")
    private String srcAdditionaltext;

    @Field(type = FieldType.Keyword)
    @JsonProperty("SRC_SYNC_NO")
    private String srcSyncNo;

    @Field(type = FieldType.Text)
    @JsonProperty("SRC_ADDITIONALINFO")
    private String srcAdditionalinfo;

    @Field(type = FieldType.Integer)
    @JsonProperty("DATA_RESOURCE")
    private Integer dataResource;

    @Field(type = FieldType.Keyword)
    @JsonProperty("APP_NAME")
    private String appName;
}
