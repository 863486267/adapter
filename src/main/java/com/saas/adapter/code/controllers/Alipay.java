package com.saas.adapter.code.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.saas.adapter.code.business.AlipayCreateBusiness;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;

@Controller
@RequestMapping("alipayCreate")
public class Alipay {

	public static final Logger log = LoggerFactory.getLogger(Alipay.class);

	@Autowired
	private AlipayCreateBusiness alipayBusiness;

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
		return alipayBusiness.pay(parameter, "ALIPAY", "WAP");
	}

	/**
	 * 支付宝下单(WAP)
	 */
	@PostMapping("createWap")
	@ResponseBody
	public OrderReturn createWap(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return alipayBusiness.payWap(parameter, "ALIPAY", "WAP");
	}

	/**
	 * 回调
	 * 
	 * @param str
	 * @return
	 * @throws Exception
	 */
	@PostMapping("notifyWap")
	@ResponseBody
	public CallbackResult notifyWap(@RequestBody String str) throws Exception {
		if (str == null || "{}".equals(str) || "".equals(str)) {
			CallbackResult callbackResult = new CallbackResult();
			callbackResult.success = false;
			callbackResult.callbackResult = "请求参数为空";
			return callbackResult;
		}
		return alipayBusiness.notifyWap(str);
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
		return alipayBusiness.pay(parameter, "ALIPAY", "scanCode");
	}

	/**
	 * 回调
	 * 
	 * @param str
	 * @return
	 */
	@PostMapping("notify")
	@ResponseBody
	public CallbackResult notify(@RequestBody String str) {
		if (str == null || "{}".equals(str) || "".equals(str)) {
			CallbackResult callbackResult = new CallbackResult();
			callbackResult.success = false;
			callbackResult.callbackResult = "请求参数为空";
			return callbackResult;
		}
		return alipayBusiness.notify(str);
	}

}
