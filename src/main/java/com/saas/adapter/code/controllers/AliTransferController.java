package com.saas.adapter.code.controllers;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.business.AliTransferBusiness;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.NotifyBankParams;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.StringSort;

@Controller
@RequestMapping("alipayNewTransfer")
public class AliTransferController {

	private static Logger LOG = LoggerFactory.getLogger(AliTransferController.class);

	@Autowired
	private AliTransferBusiness aliTransferBusiness;

	@Autowired
	private TokenClient tokenClient;

	/**
	 * 支付宝下单(WAP)
	 */
	@PostMapping("create")
	@ResponseBody
	public OrderReturn create(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return aliTransferBusiness.pay(parameter, "WAP");
	}

	/**
	 * 支付宝下单(scanCode)
	 */
	@PostMapping("scanCode")
	@ResponseBody
	public OrderReturn scanCode(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return aliTransferBusiness.pay(parameter, "SCANCODE");
	}

	@RequestMapping(value = "notifybank")
	@ResponseBody
	public String notifybank(@RequestBody NotifyBankParams notifyBankParams) {
		LOG.info("银行转账回调:" + JsonUtils.toJson(notifyBankParams));
		if (StringUtils.isBlank(notifyBankParams.bank) || StringUtils.isBlank(notifyBankParams.bankaccount)
				|| StringUtils.isBlank(notifyBankParams.money)) {
			LOG.info("回调参数缺少");
			return "fail";
		}
		StringSort stringSort = new StringSort();
		String monery = stringSort.yuan2FenInt(notifyBankParams.money);
		String notifyUrlKey = notifyBankParams.bank + notifyBankParams.bankaccount + monery;
		String notifyUrl = tokenClient.getPayParams(notifyUrlKey);
		if (StringUtils.isBlank(notifyUrl)) {
			LOG.info("该条信息未被记录");
			return "fail";
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", notifyUrlKey);
		map.put("success", true);
		aliTransferBusiness.sendNotify(map, notifyUrl);
		return "success";
	}

	/**
	 * 
	 */
	@RequestMapping(value = "notify")
	@ResponseBody
	public String notify(HttpServletRequest request) throws UnsupportedEncodingException {
		String bankphone = request.getParameter("bankphone");
		String bankmsg = request.getParameter("bankmsg");
		LOG.info("支付宝个人转账回调记录");
		LOG.info("银行电话【" + bankphone + "】");
		if (StringUtils.isBlank(bankmsg)) {
			LOG.info("短信内容【" + null + "】");
		}
		if (StringUtils.isBlank(bankmsg) || StringUtils.isBlank(bankphone)) {
			return "截取信息不足";
		}

		String msg = java.net.URLDecoder.decode(bankmsg, "UTF-8");
		LOG.info("短信内容【" + msg + "】");
		if (msg.indexOf("存入") != -1 || msg.indexOf("收入") != -1 || msg.indexOf("转账") != -1 || msg.indexOf("代付") != -1) {
			String bankNumberLastFour = msg.substring(msg.indexOf("尾号") + 2, msg.indexOf("尾号") + 6);
			String bankCode = getBankCode(bankphone);
			String monery = "";
			if (msg.indexOf("元") == -1) {
				monery = getMonery(msg.replaceAll(",", ""));
			} else {
				String strA = msg.substring(0, msg.indexOf("元"));
				if (strA.indexOf(".") == -1) {
					strA = strA + ".00";
				}
				monery = getMonery(strA.replaceAll(",", ""));
			}

			StringSort stringSort = new StringSort();
			monery = stringSort.yuan2FenInt(monery);
			String notifyUrlKey = bankCode + bankNumberLastFour + monery;
			String notifyUrl = tokenClient.getPayParams(notifyUrlKey);
			if (StringUtils.isBlank(notifyUrl)) {
				LOG.info("该条信息未被记录");
				return "fail";
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("key", notifyUrlKey);
			map.put("success", true);
			aliTransferBusiness.sendNotify(map, notifyUrl);
			return "success";
		}
		return "fail";
	}

	@PostMapping("notifyDeal")
	@ResponseBody
	public CallbackResult notifyDeal(@RequestBody String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		return aliTransferBusiness.notify(str);
	}

	private static String getMonery(String msg) {
		String[] amounts = extractAmountMsg(msg);
		String ss = "";
		StringSort stringSort = new StringSort();
		Pattern pattern = Pattern.compile("(([1-9][0-9]*)\\.([0-9]{2}))|[0]\\.([0-9]{2})");
		for (int i = 0; i < amounts.length; i++) {
			if (StringUtils.isBlank(amounts[i])) {
				continue;
			}
			Matcher match = pattern.matcher(amounts[i]);
			if (!match.matches()) {
				continue;
			}
			if (i == 0 || StringUtils.isBlank(ss)) {
				ss = amounts[i];
				continue;
			}
			if (Long.valueOf(stringSort.yuan2FenInt(ss)) - Long.valueOf(stringSort.yuan2FenInt(amounts[i])) > 0) {
				ss = amounts[i];
			}
		}
		return ss;
	}

	public static String[] extractAmountMsg(String ptCasinoMsg) {
		String returnAmounts[] = new String[4];
		ptCasinoMsg = ptCasinoMsg.replace("，", " ");
		String[] amounts = ptCasinoMsg.split(" ");
		for (int i = 0; i < amounts.length; i++) {
			Pattern p = Pattern.compile("(\\d+\\.\\d+)");
			Matcher m = p.matcher(amounts[i]);
			if (m.find()) {
				returnAmounts[i] = m.group(1);
			} else {
				p = Pattern.compile("(\\d+)");
				m = p.matcher(amounts[i]);
				if (m.find()) {
					returnAmounts[i] = m.group(1);
				}
			}
		}
		return returnAmounts;
	}

	private String getBankCode(String bankphone) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("95528", "SPDB");// 浦发银行
		map.put("19999175166", "ICBC");// 工商银行
		map.put("95588", "ICBC");// 工商银行
		map.put("9555801", "CITIC");// 中信银行
		map.put("95599", "ABC");// 中国农业银行
		if (!map.containsKey(bankphone)) {
			return null;
		}
		return map.get(bankphone);
	}
}
