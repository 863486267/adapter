package com.saas.adapter.entity;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DingDingEntity {

//    private String memberid;
//    private String orderid;
//    private String amount;
//    private String transaction_id;
//    private String datetime;
//    private String returncode;
//    private String attach;
//    private String sign;

    private String account_name;
    private String status;
    private String amount;
    private String out_trade_no;
    private String trade_no;
    private String fees;
    private String sign;
    private String callback_time;
    private String account_key;



}
