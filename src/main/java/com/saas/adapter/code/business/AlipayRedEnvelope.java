package com.saas.adapter.code.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.po.MerchantChannelAptitude;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import com.saas.adapter.tools.StringSort;

@Service
public class AlipayRedEnvelope {

	private static Logger LOG = LoggerFactory.getLogger(AlipayRedEnvelope.class);

	@Autowired
	public OrderReturnMain orderReturnMain;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

	@Autowired
	private TokenClient tokenClient;

	public OrderReturn pay(Parameter parameter ,String payType) {

		if (parameter.aptitudes.isEmpty() || parameter.aptitudes.size() == 0) {
			return orderReturnMain.failReturn("未配置交易资质参数");
		}
		// 筛选资质
		MerchantChannelAptitude aptitude = selecAptitude(parameter.aptitudes, parameter.order.money, "ALIPAY");
		if (aptitude == null) {
			return orderReturnMain.failReturn("未找到可用资质参数");
		}
		if (StringUtils.isBlank(aptitude.account)) {
			return orderReturnMain.failReturn("转账账号不能为空");
		}
		if (StringUtils.isBlank(aptitude.domain)) {
			return orderReturnMain.failReturn("支付域名地址不能为空");
		}
		try {
			// 未支付条数累计
			tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
			String remark = getId();
			tokenClient.savePayQualification(remark, JsonUtils.toJson(aptitude));
			// 保存回调地址
			tokenClient.savePayUrl(remark, parameter.order.orderConfig.notifyUrl, 50);
			Map<String, String> map = new HashMap<String, String>();
			map.put("appId", "88886666");
			map.put("appLaunchMode", "3");
			map.put("canSearch", "false");
			map.put("chatLoginId", aptitude.name);
			map.put("chatUserId", aptitude.account);
			map.put("chatUserName", aptitude.name);
			map.put("chatUserType", "1");
			map.put("entryMode", "personalStage");
			map.put("prevBiz", "chat");
			map.put("schemaMode", "portalInside");
			map.put("target", "personal");
			map.put("money", changeF2Y(parameter.order.money));
			map.put("amount", changeF2Y(parameter.order.money));
			map.put("remark", remark);
			StringSort stringSort = new StringSort();
			String str = stringSort.getUrlParamsByMap(map, true);
			// 通过订单号保存支付资质
			tokenClient.savePayParams(parameter.order.id, str);
			tokenClient.savePayParams("A" + parameter.order.id,
					aptitude.domain + "/transitNew/alipayRedEnvelope/" + parameter.order.id);
			
			if("SCANDCODE".equals(payType)) {
				return orderReturnMain.successReturn(
					 aptitude.domain + "/transitNew/aliPay/" + "A"
								+ parameter.order.id ,
						"资质账号:" + aptitude.account + ",备注：" + remark, parameter.order.money);// 返回给商户支付链接);
			}
			tokenClient.savePayParams("S-"+parameter.order.id,  aptitude.domain + "/transitNew/aliPay/" + "A"
					+ parameter.order.id);
			return orderReturnMain.successReturn(
					aptitude.domain + "/transitNew/codeUrl?id=" 
							+ parameter.order.id + "&monery=" + parameter.order.money + "&remark="+remark,
					"资质账号:" + aptitude.account + ",备注：" + remark, parameter.order.money);// 返回给商户支付链接);
		} catch (Exception e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getMessage());
		}
	}

	private MerchantChannelAptitude selecAptitude(List<MerchantChannelAptitude> merchantChannelAptitude, String money,
			String payType) {

		List<MerchantChannelAptitude> screening = new ArrayList<MerchantChannelAptitude>();

		for (MerchantChannelAptitude MerchantChannelAptitude : merchantChannelAptitude) {
			if (!MerchantChannelAptitude.enabled) {
				// 排除未激活的资质
				continue;
			}
			if (!payType.equals(MerchantChannelAptitude.type.toString())) {
				// 排除不符合交易类型的资质
				continue;
			}
			if (MerchantChannelAptitude.dayMax == 0) {
				// 未设置每日交易金额的资质直接添加
				screening.add(MerchantChannelAptitude);
				continue;
			}
			Long moneying = tokenClient.getPayMongIng(MerchantChannelAptitude.aptitudeId);
			if (MerchantChannelAptitude.dayMax - moneying - Long.valueOf(money) < 0) {
				// 排除超出日限交易的资质
				LOG.info(
						"资质名称:" + MerchantChannelAptitude.name + ",资质账号:" + MerchantChannelAptitude.account + " 额度已上限");
				continue;
			}
			screening.add(MerchantChannelAptitude);
		}
		if (screening == null || screening.size() == 0) {
			LOG.info("没有符合条件的资质");
			return null;
		}
		Gson gson = new Gson();
		int index = (int) (Math.random() * screening.size());
		try {
			// 从符合条件的集合中随机挑选一组资质
			MerchantChannelAptitude arbitration = gson.fromJson(gson.toJson(screening.get(index)),
					MerchantChannelAptitude.class);
			return arbitration;
		} catch (JsonSyntaxException e) {
			LOG.info("JSON格式转化错误--->" + e.getMessage());
			return null;
		}
	}

	/**
	 * 分转元
	 *
	 * @param amount
	 * @return
	 * @throws Exception
	 */
	private String changeF2Y(String amount) throws Exception {
		if (!amount.matches("\\-?[0-9]+")) {
			throw new Exception("分转元");
		}
		return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(100)).toString();
	}

	public static String getId() {
		int machineId = (int) (Math.random() * 9);
		int hashCodeV = UUID.randomUUID().toString().hashCode();
		if (hashCodeV < 0) {// 有可能是负数
			hashCodeV = -hashCodeV;
		}
		return machineId + String.format("%019d", hashCodeV);
	}
}
