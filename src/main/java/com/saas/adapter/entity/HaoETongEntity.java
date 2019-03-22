package com.saas.adapter.entity;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HaoETongEntity {

    private int  status;
    private int  customerid;
    private String  sdpayno;
    private String  sdorderno;
    private String  total_fee;
    private String  paytype;
    private String  remark;
    private String  sign;
}
