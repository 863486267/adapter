package com.saas.adapter.code.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.saas.adapter.code.business.AlipayHongBaoBusiness;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;

@Controller
@RequestMapping("aliPayHongBao")
public class AlipayHongBao {
	
	public static final Logger log = LoggerFactory.getLogger(AlipayHongBao.class);

	@Autowired
	private AlipayHongBaoBusiness alipayHongBaoBusiness;
	
	@PostMapping("getpermissions")
	@ResponseBody
	public Map<String, String> getPermissions(@RequestBody String str) {
		Map<String, String> map = new HashMap<>();
		map.put("no", "181201319");
		map.put("key", "97n8zz1kzc3urpv1x0k69mteq9tzo5dl");
		return map;
	}

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
		return alipayHongBaoBusiness.pay(parameter);
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
		return alipayHongBaoBusiness.notify(str);
	}

}
