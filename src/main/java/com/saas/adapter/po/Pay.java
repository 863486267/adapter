package com.saas.adapter.po;


/**
 * 代付
 * 
 * @author Administrator
 *
 */
public class Pay {

	public String id;

	/**
	 * 流水号
	 */
	public String no;

	/**
	 * 商户
	 */
	public Merchant merchant;

	/**
	 * 商户名称
	 */
	public String merchantName;

	/**
	 * 商户号
	 */
	public String merchantNo;

	/**
	 * 通道
	 */
	public Channel channel;

	/**
	 * 通道名称
	 */
	public String channelName;

	/**
	 * 通道编号
	 */
	public String channelNo;

	/**
	 * 代付金额(分)
	 */
	public Long money;

	/**
	 * 实际代付金额(分)
	 */
	public Long payMoney;

	/**
	 * 收款开户行名称
	 */
	public String bank;

	/**
	 * 收款开户行支行名称
	 */
	public String branchBankName;

	/**
	 * 收款开户行省
	 */
	public String bankProvince;

	/**
	 * 收款开户行市
	 */
	public String bankCity;

	/**
	 * 收款账户开户人名称
	 */
	public String cardName;

	/**
	 * 收款账户号
	 */
	public String cardNo;

	/**
	 * 收款账户开户人身份证号
	 */
	public String cardId;

	/**
	 * 收款卡清算大行号
	 */
	public String bankNo;

	/**
	 * 收款卡清算支行号
	 */
	public String inerBankNo;

	/**
	 * 收款人手机号
	 */
	public String phone;

	/**
	 * 代付用途
	 */
	public String detail;

	/**
	 * 上游流水号
	 */
	public String tradeNo;

	/**
	 * 下游商户号
	 */
	public String outTradeNo;

	/**
	 * 生成状态(true：生成成功，false：生成失败)
	 */
	public Boolean success;

	/**
	 * 代付状态(true：代付到账成功，false：代付到账失败)
	 */
	public Boolean pay;

	/**
	 * 代付完成日期
	 */
	public String payDate;

	/**
	 * 适配器地址
	 */
	public String url;

	/**
	 * 代付回调适配器地址
	 */
	public String callbackUrl;

	/**
	 * 适配器代付状态查询地址
	 */
	public String payStateUrl;

	/**
	 * 代付通知上游适配器地址
	 */
	public String notifyUrl;

	/**
	 * 代付下游通知地址
	 */
	public String notifyOutUrl;

	/**
	 * 是否通知下游(NULL：未通知单，true：通知成功，false：通知失败)
	 */
	public Boolean notify;

	/**
	 * 通知日期
	 */
	public String notifyDate;

	/**
	 * 代付费率(‰)
	 */
	public Double rate;

	/**
	 * 代付成本费率(‰)
	 */
	public Double costRate;

	/**
	 * 支付通道费率(%)
	 */
	public Double channelRate;

	/**
	 * 代付固定手续费(分)
	 */
	public Long poundage;

	/**
	 * 代付成本手续费(分)
	 */
	public Long costPoundage;

	/**
	 * 支付通道代付手续费(分)
	 */
	public Long channelPoundage;

	/**
	 * 上限手续费(分)
	 */
	public Long limit;

	/**
	 * 上限成本手续费(分)
	 */
	public Long costLimit;

	/**
	 * 支付通道上限手续费(分)
	 */
	public Long channelLimit;

	/**
	 * 下限手续费(分)
	 */
	public Long minimum;

	/**
	 * 下限成本手续费(分)
	 */
	public Long costMinimum;

	/**
	 * 支付通道下限手续费(分)
	 */
	public Long channelMinimum;

	/**
	 * 回调结果
	 */
	public String callbackResult;

	/**
	 * 响应信息
	 */
	public String resultMessage;

	/**
	 * 响应码
	 */
	public String resultCode;

	/**
	 * 下单日期
	 */
}
