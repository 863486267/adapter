package com.saas.adapter.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BaoTianEntity {
    private String p_Amount;
    private String p_ChildChannelNo;
    private String p_MainChannelNo;
    private String p_MerchantNo;
    private String p_OrderNo;
    private String p_PayHtml;
    private String sign;
}
