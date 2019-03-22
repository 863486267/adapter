package com.saas.adapter.entity;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NotifyEntity {
    private String code;
    private String message;
    private String externalId;
    private String seqno;
    private String amount;
    private String customerNo;
    private String selfData;
    private String sign;
}
