package com.saas.adapter.code.business;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.dto.ShunLongNotifyParams;
import com.saas.adapter.code.dto.ShunLongParams;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.MD5;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import com.saas.adapter.tools.StringSort;

@Component
public class ShunLongPayBusiness {

	@Autowired
	public OrderReturnMain orderReturnMain;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

	@Autowired
	private TokenClient tokenClient;

	public OrderReturn pay(Parameter parameter) {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String bankCode = "903";
		String subject = "";
		if (parameter.order.detail != null) {
			subject = parameter.order.detail;
		} else if (parameter.order.body != null) {
			subject = parameter.order.body;
		} else {
			subject = "支付宝支付";
		}
		ShunLongParams shunLongParams = new ShunLongParams();
		shunLongParams.payMemberid = parameter.merchantChannel.no;
		shunLongParams.payOrderid = parameter.order.no;
		shunLongParams.payApplydate = date;
		shunLongParams.payBankcode = bankCode;
		tokenClient.savePayUrl(parameter.order.no, parameter.order.orderConfig.notifyUrl, 1440);// 记录异步通知地址
		shunLongParams.payNotifyurl = "http://adapter.ivpai.com/shunLongController/notify";
		shunLongParams.payCallbackurl = parameter.order.orderConfig.successUrl;
		try {
			if (isContainChinese(subject)) {
				shunLongParams.payProductname = java.net.URLDecoder.decode(subject, "UTF-8");
				;
			} else {
				shunLongParams.payProductname = subject;
			}
			StringSort stringSort = new StringSort();
			shunLongParams.payAmount = stringSort.changeF2Y(parameter.order.money);
			String stringSignTemp = "pay_amount=" + shunLongParams.payAmount + "&pay_applydate=" + date
					+ "&pay_bankcode=" + bankCode + "&pay_callbackurl=" + parameter.order.orderConfig.successUrl
					+ "&pay_memberid=" + parameter.merchantChannel.no + "&pay_notifyurl=" + shunLongParams.payNotifyurl
					+ "&pay_orderid=" + parameter.order.no + "&key=" + parameter.merchantChannel.key + "";
			String sign = MD5.sign(stringSignTemp, "", "utf-8").toUpperCase();
			shunLongParams.payMd5sign = sign;
		} catch (Exception e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getMessage());
		}
		tokenClient.savePayParams(parameter.order.id, JsonUtils.toJson(shunLongParams));
		return orderReturnMain.successReturn("http://adapter.ivpai.com/transitNew/shunLong/" + parameter.order.id,
				"预下单成功", parameter.order.money);
	}

	public CallbackResult notify(String str) {
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
		String money = String.valueOf(orderMap.get("money"));
		String body = String.valueOf(paramMap.get("body"));
		Map<?,?> orderConfig = (Map<?, ?>) orderMap.get("orderConfig");
		String key = String.valueOf(orderConfig.get("key"));
		ShunLongNotifyParams shunLongNotifyParams = gson.fromJson(body, ShunLongNotifyParams.class);
		StringSort stringSort = new StringSort();
		String nowMoney = stringSort.yuan2FenInt(shunLongNotifyParams.amount);
		if (!nowMoney.equals(money.substring(0, money.indexOf(".")))) {
			return saasNotifyParams.errorParams("订单金额不正确", "OK");
		}
		String signTemp = "amount=" + shunLongNotifyParams.amount + "+datetime=" + shunLongNotifyParams.datetime + "+memberid=" + shunLongNotifyParams.memberid + "+orderid=" + shunLongNotifyParams.orderid
				+ "+returncode=" + shunLongNotifyParams.returncode + "+transaction_id=" + shunLongNotifyParams.transaction_id + "+key=" + key + "";
		String sign = "";
		try {
			sign = MD5.sign(signTemp, "", "utf-8").toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if ("00".equals(shunLongNotifyParams.returncode)&&sign.equals(shunLongNotifyParams.sign)) {
			return saasNotifyParams.successParams(shunLongNotifyParams.transaction_id, "OK");
		}
		return null;

	}

	/**
	 * 判断字符串中是否包含中文
	 * 
	 * @param str 待校验字符串
	 * @return 是否为中文
	 * @warn 不能校验是否为中文标点符号
	 */
	public static boolean isContainChinese(String str) {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

}
