package com.saas.adapter.po;

/**
 * 商户通道资质
 * 
 * @author Administrator
 *
 */
public class MerchantChannelAptitude {

	public String aptitudeId;
	/**
	 * 商户通道
	 */
	public MerchantChannel merchantChannel;

	/**
	 * 资质类型(微信，支付宝，QQ)
	 */
	public AptitudeType type;

	/**
	 * 资质名称
	 */
	public String name;

	/**
	 * 资质登录帐号
	 */
	public String account;

	/**
	 * 资质支付地址
	 */
	public String url;

	/**
	 * 资质当日限额(分)
	 */
	public Long dayMax;

	/**
	 * 二维码域名
	 */
	public String domain;

	/**
	 * 资质状态/true 正常 false 停用
	 */
	public Boolean enabled;

	/**
	 * 风控限制(条)
	 */
	public Integer count;
	
	/**
	 * 支付宝公钥
	 */
	public String aliPublicKey;
}
