package com.saas.adapter.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NingMnegEntity {

    private String sign;
    private String amount;
    private String attach;
    private String datetime;
    private String memberid;
    private String orderid;
    private String returncode;
    private String transaction_id;
}
