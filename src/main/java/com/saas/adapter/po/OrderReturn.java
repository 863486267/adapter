package com.saas.adapter.po;


/**
 * 下单同步返回 
 * @author Administrator
 *
 */
public class OrderReturn {
	/**
	 * 支付地址
	 */
	public String url ;
	/**
	 * 状态
	 */
	public boolean success;
	/**
	 * 下单详情
	 */
	public String resultMessage;
	/**
	 * 实际交易金额
	 */
	public String factMoney;
	
}
