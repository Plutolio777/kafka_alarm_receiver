package com.example.kafka_alarm_receiver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AlarmMessage {

    @JsonProperty("SRC_ACKNOWLEDGEMENTTIMESTAMP")
    private String srcAcknowledgementtimestamp;

    @JsonProperty("SRC_ALARM_NTIME")
    private String srcAlarmNtime;

    @JsonProperty("SRC_PERCEIVEDSEVERITY")
    private Integer srcPerceivedseverity;

    @JsonProperty("SRC_MANU_ALARMFLAG")
    private String srcManuAlarmflag;

    @JsonProperty("SRC_EVENTTIME")
    private String srcEventtime;

    @JsonProperty("SRC_LOCATIONINFO")
    private String srcLocationinfo;

    @JsonProperty("SRC_ALARMCODE")
    private String srcAlarmcode;

    @JsonProperty("SRC_ALARMID")
    private Long srcAlarmid;

    @JsonProperty("SRC_NMS_TYPE")
    private String srcNmsType;

    @JsonProperty("SRC_SYNC_FLAG")
    private String srcSyncFlag;

    @JsonProperty("SRC_ALARMSUBTYPE")
    private String srcAlarmsubtype;

    @JsonProperty("SRC_ALARM_NID")
    private String srcAlarmNid;

    @JsonProperty("sCT")
    private String sct;

    @JsonProperty("SRC_ALARM_PROBLEM")
    private String srcAlarmProblem;

    @JsonProperty("SRC_SPECIALTY")
    private String srcSpecialty;

    @JsonProperty("SRC_ID")
    private String srcId;

    @JsonProperty("SRC_SYSTEM_TYPE")
    private String srcSystemType;

    @JsonProperty("sPS")
    private String sps;

    @JsonProperty("SRC_ALARM_NO")
    private String srcAlarmNo;

    @JsonProperty("SRC_REGION")
    private String srcRegion;

    @JsonProperty("SRC_SYSTEM_DN")
    private String srcSystemDn;

    @JsonProperty("SRC_ACKSTATUS")
    private String srcAckstatus;

    @JsonProperty("SRC_IS_TEST")
    private Boolean srcIsTest;

    @JsonProperty("SRC_ALARMTITLE")
    private String srcAlarmtitle;

    @JsonProperty("SRC_INFO9")
    private String srcInfo9;

    @JsonProperty("SRC_IP")
    private String srcIp;

    @JsonProperty("SRC_EQUIPMENTNAME")
    private String srcEquipmentname;

    @JsonProperty("SRC_INERTTIME")
    private String srcInerttime;

    @JsonProperty("SRC_SOURCE")
    private String srcSource;

    @JsonProperty("SRC_ALARMTYPE")
    private String srcAlarmtype;

    @JsonProperty("SRC_CLEARANCEREPORTFLAG")
    private Boolean srcClearancereportflag;

    @JsonProperty("SRC_IPADDRESS")
    private String srcIpaddress;

    @JsonProperty("SRC_STATE")
    private Integer srcState;

    @JsonProperty("SRC_NAME")
    private String srcName;

    @JsonProperty("SRC_vendor_ALARMID")
    private String srcVendorAlarmid;

    @JsonProperty("SRC_INFO8")
    private String srcInfo8;

    @JsonProperty("SRC_INFO7")
    private String srcInfo7;

    @JsonProperty("SRC_INFO6")
    private String srcInfo6;

    @JsonProperty("SRC_INFO5")
    private String srcInfo5;

    @JsonProperty("SRC_NECLASS")
    private String srcNeclass;

    @JsonProperty("SRC_CLEARANCETIMESTAMP")
    private String srcClearancetimestamp;

    @JsonProperty("SRC_INFO4")
    private String srcInfo4;

    @JsonProperty("SRC_INFO3")
    private String srcInfo3;

    @JsonProperty("sEN")
    private String sen;

    @JsonProperty("SRC_INFO2")
    private String srcInfo2;

    @JsonProperty("SRC_INFO1")
    private String srcInfo1;

    @JsonProperty("SRC_RESTORETYPE")
    private String srcRestoretype;

    @JsonProperty("SRC_ORG_CLR_OPTR")
    private String srcOrgClrOptr;

    @JsonProperty("SRC_APP_ID")
    private String srcAppId;

    @JsonProperty("SRC_ALARMSID")
    private String srcAlarmsid;

    @JsonProperty("SRC_VENDOR")
    private String srcVendor;

    @JsonProperty("SRC_ALARM_NTYPE")
    private String srcAlarmNtype;

    @JsonProperty("SRC_NETTYPE")
    private String srcNettype;

    @JsonProperty("SRC_SENDTIME")
    private String srcSendtime;

    @JsonProperty("SRC_MANU_ALARMTYPE")
    private String srcManuAlarmtype;

    @JsonProperty("SRC_ADDITIONALTEXT")
    private String srcAdditionaltext;

    @JsonProperty("SRC_SYNC_NO")
    private String srcSyncNo;

    @JsonProperty("SRC_ADDITIONALINFO")
    private String srcAdditionalinfo;

    @JsonProperty("DATA_RESOURCE")
    private Integer dataResource;

    @JsonProperty("APP_NAME")
    private String appName;
}
