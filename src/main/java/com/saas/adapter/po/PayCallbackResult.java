package com.saas.adapter.po;
/**
 *  异步返回结果
 * @author Administrator
 *
 */
public class PayCallbackResult {
	/**
	 * 回调结果
	 */
	public String callbackResult;

	/**
	 * 回调响应
	 */
	public String callbackResponse;

	/**
	 * 上游单号
	 */
	public String tradeNo;

	public boolean success;

	public String companyId;

	public String merchantId;

	public String distributorId;

	public String no;
}
