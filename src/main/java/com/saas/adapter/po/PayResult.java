package com.saas.adapter.po;

/**
 * 代付同步回调参数
 * @author Administrator
 *
 */
public class PayResult {
	/**
	 * 成功
	 */
	public boolean success;

	/**
	 * 上游流水号
	 */
	public String tradeNo;

	/**
	 * 商户
	 */
	public String outTradeNo;

	/**
	 * 系统单号
	 */
	public String no;

	/**
	 * 响应信息
	 */
	public String resultMessage;

	/**
	 * 响应码
	 */
	public String resultCode;
}
