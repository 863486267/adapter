package com.saas.adapter.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PddEntity {
    private String memberid;
    private String orderid;
    private String amount;
    private String transaction_id;
    private String datetime;
    private String returncode;
    private String attach;
    private String sign;
}
