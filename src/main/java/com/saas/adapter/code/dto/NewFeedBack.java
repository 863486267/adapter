package com.saas.adapter.code.dto;

/**
 * 新App监控同步返回参数
 * @author Administrator
 *
 */
public class NewFeedBack {
	/**
	 * 提示信息
	 */
	public String msg;
	/**
	 * 返回code 失败：0 成功：1
	 */
	public String code;
	/**
	 * 生成金额
	 */
	public String money;
	/**
	 * 收款账号余额
	 */
	public String balance;
	/**
	 * 返回二维码内容
	 */
	public String payurl;
	/**
	 * 生成类型
	 */
	public String type;
	/**
	 * 生成备注
	 */
	public String mark;
	/**
	 * 收款账号
	 */
	public String account;

}
