package com.saas.adapter.code.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.business.ShunLongPayBusiness;
import com.saas.adapter.code.dto.ShunLongNotifyParams;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;

@Controller
@RequestMapping("shunLongController")
public class ShunLongPayController {

	@Autowired
	private ShunLongPayBusiness shunLongPayBusiness;

	@Autowired
	private RestTemplate restTemplate;

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
		return shunLongPayBusiness.pay(parameter);
	}

	@PostMapping("notifyDeal")
	@ResponseBody
	public CallbackResult notifyDeal(@RequestBody String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		return shunLongPayBusiness.notify(str);
	}

	
	/**
	 * 异步通知地址接收数据（固定）
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "notify")
	@ResponseBody
	public String notify(HttpServletRequest request) {
		ShunLongNotifyParams shunLongNotifyParams = new ShunLongNotifyParams();
		shunLongNotifyParams.memberid = request.getParameter("memberid");
		shunLongNotifyParams.orderid = request.getParameter("orderid");
		shunLongNotifyParams.amount = request.getParameter("amount");
		shunLongNotifyParams.transaction_id = request.getParameter("transaction_id");
		shunLongNotifyParams.datetime = request.getParameter("datetime");
		shunLongNotifyParams.returncode = request.getParameter("returncode");
		shunLongNotifyParams.attach = request.getParameter("attach");
		shunLongNotifyParams.sign = request.getParameter("sign");

		if (StringUtils.isBlank(shunLongNotifyParams.orderid)) {
			return "FAIL";
		}
		String notifyUrl = tokenClient.getPayParams(shunLongNotifyParams.orderid);//取出SAAS回调地址
		if (StringUtils.isBlank(notifyUrl)) {
			return "FAIL";
		}
		restTemplate.postForObject(notifyUrl, shunLongNotifyParams, String.class);
		return "Ok";
	}

}
