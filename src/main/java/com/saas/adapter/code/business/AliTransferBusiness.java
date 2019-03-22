package com.saas.adapter.code.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.MerchantChannelAptitude;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import com.saas.adapter.tools.StringSort;

@Service
public class AliTransferBusiness {

	private static Logger LOG = LoggerFactory.getLogger(AliTransferBusiness.class);

	@Autowired
	public OrderReturnMain orderReturnMain;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

	@Autowired
	private TokenClient tokenClient;

	@Autowired
	private RestTemplate restTemplate;

	public CallbackResult notify(String str) {
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
		String body = String.valueOf(paramMap.get("body"));
		String money = String.valueOf(orderMap.get("money"));
		Map<?, ?> notifyDto = gson.fromJson(body, Map.class);
		if (!notifyDto.containsKey("key") || !notifyDto.containsKey("success")) {
			return null;
		}
		if ("false".equals(notifyDto.get("success"))) {
			return null;
		}
		String madeForAptitude = tokenClient.getQualification(orderMap.get("id").toString());
		MerchantChannelAptitude aptitude = JsonUtils.toObject(madeForAptitude, MerchantChannelAptitude.class);
		// 累加交易金额
		tokenClient.setMoney(aptitude.aptitudeId, money.substring(0, money.indexOf(".")));
		// 移除订单资质信息
		tokenClient.removeQualificationByOrder(orderMap.get("id").toString());
		// 移除资质为支付累加条数
		tokenClient.removeByNumber(aptitude.aptitudeId);
		// 已支付的订单移除交易支付链接
		tokenClient.remove(orderMap.get("id").toString());
		// 已支付的订单移除交易通知地址
		tokenClient.remove(notifyDto.get("key").toString());
		return saasNotifyParams.successParams("", "success");
	}

	public OrderReturn pay(Parameter parameter, String payType) {
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
		if (StringUtils.isBlank(aptitude.url)) {
			return orderReturnMain.failReturn("银行编码不能为空");
		}
		if (StringUtils.isBlank(aptitude.domain)) {
			return orderReturnMain.failReturn("支付域名地址不能为空");
		}
		try {
			// 金额处理【随机立减】
			String monery = "";
			String notifyUrlKey = "";
			String notifyUrl = "";

			for (int i = 0; i < 30; i++) {
				monery = getRandomKnock(parameter.order.money, aptitude.aptitudeId + parameter.order.money);
				monery = getReduction(monery, getPoundAge(monery));
				notifyUrlKey = aptitude.url
						+ aptitude.account.substring(aptitude.account.length() - 4, aptitude.account.length()) + monery;
				notifyUrl = tokenClient.getPayParams(notifyUrlKey);
				if (StringUtils.isBlank(notifyUrl)) {
					LOG.info("该随机立减金额未被使用(分)---" + monery + ",key:" + notifyUrlKey);
					break;
				}
				LOG.info("该随机立减金额被占用(分)---" + monery + ",key:" + notifyUrlKey);
			}
			if (StringUtils.isNotBlank(notifyUrl)) {
				return orderReturnMain.failReturn("暂无可用资金金额,请稍后重试.");
			}
			String moneryYuan = changeF2Y(monery);
			// 未支付条数累计
			tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
			// 通过订单号保存支付资质
			tokenClient.savePayQualification(parameter.order.id, JsonUtils.toJson(aptitude));
			// 保存回调地址
			LOG.info("回调地址保存:" + notifyUrlKey + ",地址:" + parameter.order.orderConfig.notifyUrl);
			tokenClient.savePayParams(notifyUrlKey, parameter.order.orderConfig.notifyUrl);
			Map<String, String> map = new HashMap<String, String>();
			map.put("appId", "09999988");
			map.put("actionType", "toCard");
			map.put("sourceId", "bill");
			String bankAccount = aptitude.name.substring(0, aptitude.name.indexOf("-"));
			String cardIndex = aptitude.name.substring(aptitude.name.indexOf("-") + 1, aptitude.name.length());
			map.put("cardNo", "****************");
			map.put("cardIndex", cardIndex);
			map.put("bankAccount", bankAccount);
			map.put("money", moneryYuan);
			map.put("amount", moneryYuan);
			map.put("bankMark", aptitude.url);
			map.put("cardNoHidden", "true");
			map.put("cardChannel", "HISTORY_CARD");
			map.put("orderSource", "from");
			StringSort stringSort = new StringSort();
			String str = stringSort.getUrlParamsByMap(map, true);

			if ("WAP".equals(payType)) {
//				String url = "alipays://platformapi/startapp?" + str;
//				boolean savePayParamsTwo = tokenClient.savePayUrl("aliPaySecondStep." + parameter.order.id,
//						aptitude.domain + "/transitNew/payNew/" + parameter.order.id);// 保存支付地址
//				boolean savePayParamsThree = tokenClient.savePayUrl(parameter.order.id, url);// 保存支付地址
//				if (!savePayParamsTwo || !savePayParamsThree) {
//					return orderReturnMain.failReturn("订单预处理失败(00003)");
//				}
//				return orderReturnMain.successReturn(
//						aptitude.domain + "/transitNew/aliPaySecondStep/" + parameter.order.id,
//						"资质账号:" + aptitude.account, monery);// 返回给商户支付链接);
				String url = "http://47.106.220.74:8181/pay/index?amount=" + monery + "&bankAccount=" + bankAccount
						+ "&bankMark=" + aptitude.url + "&cardIndex=" + cardIndex;
				boolean savePayParamsTwo = tokenClient.savePayUrl(parameter.order.id, url,10);// 保存支付地址
				if (!savePayParamsTwo) {
					return orderReturnMain.failReturn("订单预处理失败(00003)");
				}
				return orderReturnMain.successReturn(
						aptitude.domain + "/transitNew/payNew/" + parameter.order.id,
						"资质账号:" + aptitude.account, monery);// 返回给商户支付链接);

			}
			if ("SCANCODE".equals(payType)) {
				String url = "https://www.alipay.com?" + str;
				boolean savePayParamsTwo = tokenClient.savePayUrl(parameter.order.id, url,10);// 保存支付地址
				if (!savePayParamsTwo) {
					return orderReturnMain.failReturn("订单预处理失败(00003)");
				}
				return orderReturnMain.successReturn(aptitude.domain + "/transitNew/payNew/" + parameter.order.id,
						"资质账号:" + aptitude.account, monery);// 返回给商户支付链接);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getMessage());
		}
		return null;
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

	public void sendNotify(Object notifyDto, String url) {
		restTemplate.postForObject(url, notifyDto, String.class);
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

	private String getRandomKnock(String monery, String AptitudeId) {
		String randomKnockMonery = tokenClient.getMoneryRandomKnock(AptitudeId);
		LOG.info("立减金额数(分):" + randomKnockMonery);
		return Long.toString((Long.valueOf(monery) - Long.valueOf(randomKnockMonery)));
	}

	private String getPoundAge(String monery) {
		BigDecimal numOne = new BigDecimal(monery);
		BigDecimal numTwo = new BigDecimal("0.001");
		BigDecimal result = numOne.multiply(numTwo);
		return String.valueOf(result.intValue());
	}

	private String getReduction(String moneryOne, String moneryTwo) {
		BigDecimal numOne = new BigDecimal(moneryOne);
		BigDecimal numTwo = new BigDecimal(moneryTwo);
		BigDecimal result = numOne.subtract(numTwo);
		return String.valueOf(result.intValue());
	}
}
