package com.saas.adapter.alipay.test;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.saas.adapter.alipay.dto.OrderResult;


/**
 * demo适配器
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("test")
public class TestAlipay {

	private static final String URL = "http://paychannelone.appli.xjockj.com/alipay/getPay";

	@RequestMapping("forward")
	public String forward(String app_id, String state, String scope, String auth_code, Model model) {
		if(auth_code == null){
			model.addAttribute("message", "请重新下单");
			return "error";
		}
		Map<String, String> map = new HashMap<>();
		map.put("app_id", app_id);
		map.put("state", state);
		map.put("scope", scope);
		map.put("auth_code", auth_code);
		RestTemplate restTemplate = new RestTemplate();
		OrderResult result = restTemplate.postForObject(URL, map, OrderResult.class);
		if (result.success) {
			model.addAttribute("payInfo", result.tradeNo);
			return "alipay";
		}
		if (!result.success && StringUtils.isNotBlank(result.resultMessage)) {
			model.addAttribute("message", result.resultMessage);
			return "error";

		}
		return "error";
	}

}
