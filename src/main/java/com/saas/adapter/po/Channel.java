package com.saas.adapter.po;



/**
 * 通道
 * 
 * @author Administrator
 *
 */
public class Channel {


	public String id;

	/**
	 * 通道编号
	 */
	public String no;

	/**
	 * 通道名称
	 */
	public String name;

	/**
	 * 权重(优先级别)
	 */
	public Integer weight;
	
	/**
	 * 资质轮巡池
	 */
	public String param;

	/**
	 * 支付接口
	 */
//	@JsonIgnore
//	public List<PayChannel> channels;

//	/**
//	 * 机构通道
//	 */
//	@JsonIgnore
//	public List<CompanyChannel> companyChannels;

	/**
	 * 适配器进件接口URL
	 */
	public String registerUrl;

	/**
	 * 适配器提现接口URL
	 */
	public String cashUrl;

	/**
	 * 提现费率(%)
	 */
	public Double cashRate;

	/**
	 * 提现固定手续费(%)
	 */
	public Long cashMoney;

	/**
	 * 提现上限手续费(%)
	 */
	public Long cashLimit;

	/**
	 * 提现下限手续费(%)
	 */
	public Long cashMinimum;

	/**
	 * 适配器自动对账地址
	 */
	public String checkUrl;

	/**
	 * 适配器退款地址
	 */
	public String refundUrl;

	/**
	 * 健康状体监控URL
	 */
	public String healthUrl;

	/**
	 * 是否开启健康监测
	 */
	public Boolean healthEnabled;

	/**
	 * 通道健康状体监控URL
	 */
	public String adapterHealthUrl;

	/**
	 * 是否开启通道健康监测
	 */
	public Boolean adapterHealthEnabled;

	/**
	 * 适配器退款回调地址
	 */
	public String refundCallbackUrl;

	/**
	 * 公司
	 */
//	@JsonIgnore
//	public Company company;

	/**
	 * 是否开启
	 */
	public Boolean enabled;

	/**
	 * 录入日期
	 */
	public String addedDate;

	/**
	 * 开户人姓名
	 */
	public String cardName;

	/**
	 * 卡号
	 */
	public String cardNo;

	/**
	 * 大行名称
	 */
	public String bankName;

	/**
	 * 大行号
	 */
	public String bankNo;

	/**
	 * 联行号
	 */
	public String bankCode;

	/**
	 * 开户行名称
	 */
	public String bank;

	/**
	 * 开户行地址
	 */
	public String bankAddress;

	/**
	 * 适配器代付接口URL
	 */
	public String payUrl;

	/**
	 * 适配器代付回调接口URL
	 */
	public String payCallbackUrl;
	
	/**
	 * 适配器代付状态查询URL
	 */
	public String payStateUrl;

	/**
	 * 是否开启代付
	 */
	public Boolean payEnabled;

	/**
	 * 代付费率(%)
	 */
	public Double payRate;

	/**
	 * 代付固定手续费(%)
	 */
	public Long payMoney;
	/**
	 * 单笔代付上限金额
	 */
	public Long payUpperMoney;

	/**
	 * 代付上限手续费(%)
	 */
	public Long payLimit;

	/**
	 * 代付下限手续费(%)
	 */
	public Long payMinimum;

	/**
	 * 交易代付冻结比例(%)
	 */
	public Double tradePayFrostPercent;
}
