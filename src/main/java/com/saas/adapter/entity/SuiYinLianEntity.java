package com.saas.adapter.entity;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SuiYinLianEntity {
    private String agency_id;
    private String provider_id;
    private String service;
    private String nonce_str;
    private String sign;
    private String trade_no;
    private String trade_status;
    private String transaction_id;
    private String trade_barcode;
    private int fee;
    private int cash;
    private int coupon;
    private String bank_type;
    private String payed_time;
}
