package com.saas.adapter.code.controllers;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.business.AlipayPhoneBusiness;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.HuaBuDianNotifyParams;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.SaasNotifyParams;

@Controller
@RequestMapping("huaBuDianController")
public class HuaBuDianController {

	@Autowired
	private AlipayPhoneBusiness alipayPhoneBusiness;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

	@Autowired
	private TokenClient tokenClient;

	@PostMapping("getpermissions")
	@ResponseBody
	public Map<String, String> getPermissions(@RequestBody String str) {
		Map<String, String> map = new HashMap<>();
		map.put("no", "490255822231");
		map.put("key", "216d6dd41800afc263f8736ed6e75da2");
		return map;
	}

	/**
	 * 支付宝下单(WAP)
	 */
	@PostMapping("aliwap")
	@ResponseBody
	public OrderReturn aliWap(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return alipayPhoneBusiness.payWap(parameter, "aliwap");
	}

	/**
	 * 支付宝下单(WAP)
	 */
	@PostMapping("alih5")
	@ResponseBody
	public OrderReturn alih5(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return alipayPhoneBusiness.payWap(parameter, "alih5");
	}

	/**
	 * 支付宝下单(aliQrcode)
	 */
	@PostMapping("aliqrcode")
	@ResponseBody
	public OrderReturn aliQrcode(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return alipayPhoneBusiness.payWap(parameter, "aliqrcode");
	}

	// adapter.ivpai.com

	@SuppressWarnings("finally")
	@PostMapping("notify")
	@ResponseBody
	public String notify(HttpServletRequest request) throws UnsupportedEncodingException {

		String ret = "SUCCESS";
		request.setCharacterEncoding("UTF-8");// 针对的post方式提交的乱码问题
		String str = getParamter2(request);
		Gson gson = new Gson();
		HuaBuDianNotifyParams huaBuDianNotifyParams = gson.fromJson(str, HuaBuDianNotifyParams.class);
		String notifyUrl = tokenClient.getPayParams(huaBuDianNotifyParams.extra);
		try {
			if ("1".equals(huaBuDianNotifyParams.status) || StringUtils.isNotBlank(notifyUrl)) {
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.postForObject(notifyUrl, huaBuDianNotifyParams, String.class);
			}
		} catch (RestClientException ex) {
			ex.printStackTrace();
		} finally {
			return ret;
		}
	}

	@PostMapping("notifyDeal")
	@ResponseBody
	public CallbackResult notifyDeal(@RequestBody String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		String body = String.valueOf(paramMap.get("body"));
		HuaBuDianNotifyParams huaBuDianNotifyParams = gson.fromJson(body, HuaBuDianNotifyParams.class);
		if ("1".equals(huaBuDianNotifyParams.status)) {
			return saasNotifyParams.successParams(huaBuDianNotifyParams.orderNo, "SUCCESS");
		}
		return null;
	}
	
	

	@PostMapping("notifyDealA")
	@ResponseBody
	public CallbackResult notifyDealA(@RequestBody String str) {
			return saasNotifyParams.successParams("", "SUCCESS");
	}

	/**
	 * @描述：获取参数的方法二 @时间：2017年6月22日02:06:46 @注意：
	 * @作者：Ckinghan
	 * @param req
	 */
	private String getParamter2(HttpServletRequest req) {
		// 通过.getParameterNames()获取所有的参数名称
		Enumeration<String> parameterNames = req.getParameterNames();
		// 遍历参数名称
		StringBuffer str = new StringBuffer();
		while (parameterNames.hasMoreElements()) {
			// 获取当前循环的参数名称
			String nextElement = parameterNames.nextElement();
			// 根据当前的参数名称获取对应的值，因考虑到会接收checkbox类型的，所以使用了getParameterValues()方法返回数组
			String[] parameterValues = req.getParameterValues(nextElement);
			// 将对应的参数值遍历输出
			for (int i = 0; parameterValues != null && i < parameterValues.length; i++) {
				str.append(nextElement);
			}
		}
		return str.toString();
	}
}
