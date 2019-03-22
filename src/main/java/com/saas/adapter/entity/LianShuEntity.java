package com.saas.adapter.entity;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LianShuEntity {
    private String transCode;
    private String service;
    private String reqDate;
    private String transAmount;
    private String bgReturnUrl;
    private String openId;
    private String reqTime;
    private String customerNo;
    private String externalId;
    private String requestIp;
    private String sign;

}
