package com.saas.adapter.entity;


import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class JiaLianEntity {

    private String returncode;
    private String datetime;
    private String memberid;
    private String orderid;
    private String amount;
    private String attach;
    private String transaction_id;
    private String sign;
}
