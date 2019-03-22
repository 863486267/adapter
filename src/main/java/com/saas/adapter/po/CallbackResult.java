package com.saas.adapter.po;

/**
 * 订单异步 返回SAAS实体
 * @author Administrator
 *
 */
public class CallbackResult {

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

	/**
	 * 通道可用性(false：不可用，其他是可用)
	 */
	public Boolean available;
	
	/**
	 * 上有返回错误码
	 */
	public String  resultCode;
	
	/**
	 * 上有 返回错误信息
	 */
	public String resultMessage;

}
