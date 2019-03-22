package com.saas.adapter.entity;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class YouJiaNewEntity {
    private String income;
    private String payOrderId;
    private String mchId;
    private String appId;
    private String productId;
    private String mchOrderNo;
    private String amount;
    private String status;
    private String channelOrderNo;
    private String channelAttach;
    private String param1;
    private String param2;
    private String paySuccTime;
    private String sign;
}
