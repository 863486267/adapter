package com.saas.adapter.alipay.dto;

public class OrderResult {
	
	/**
	 * 付款链接
	 */
	public String url;

	/**
	 * 系统单号
	 */
	public String no;

	/**
	 * 上游单号
	 */
	public String tradeNo;

	/**
	 * 商户单号
	 */
	public String outTradeNo;

	/**
	 * 响应信息
	 */
	public String resultMessage;

	/**
	 * 响应码
	 */
	public String resultCode;

	/**
	 * 金额
	 */
	public long money;

	/**
	 * 回调方式(ture:已付款,false:未付款，null:付款中)
	 */
	public Boolean pay;

	/**
	 * 成功
	 */
	public boolean success;
}
