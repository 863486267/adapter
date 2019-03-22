package com.saas.adapter.code.controllers;

import java.util.HashMap;
import java.util.Map;

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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.saas.adapter.code.business.CodePayBusiness;
import com.saas.adapter.code.dto.NotifyDto;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;

/**
 * 云端自动生码下单接口
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("codePay")
public class CodePay {

	private static Logger LOG = LoggerFactory.getLogger(CodePay.class);

	@Autowired
	private CodePayBusiness codePayBusiness;

//	@PostMapping("createAlipay")
//	public OrderReturn create(@RequestBody String str) {
//		
//		LOG.info("下单参数:"+str);
//		OrderReturn orderReturn = new OrderReturn();
//		orderReturn.success = false;
//		orderReturn.resultMessage = "我在测试";
//		return orderReturn;
//	}
	// 老版
	@PostMapping("createAlipay")
	@ResponseBody
	public OrderReturn create(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "ALIPAY", "old");
	}

	// 新版
	@PostMapping("createAlipayS")
	@ResponseBody
	public OrderReturn createS(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "ALIPAY", "new");
	}
	
	
	// 新版
	@PostMapping("createAlipayNew")
	@ResponseBody
	public OrderReturn createAlipayNew(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "ALIPAY", "newPay");
	}

	// 上游A模式
	@PostMapping("createAlipayUpstreamA")
	@ResponseBody
	public OrderReturn createAlipayUpstreamA(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "ALIPAY", "UpstreamA");
	}

	@PostMapping("createWechatUpstreamA")
	@ResponseBody
	public OrderReturn createWechatUpstreamA(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "WECHAT", "UpstreamA");
	}

	@PostMapping("createWechat")
	@ResponseBody
	public OrderReturn createWechat(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "WECHAT", "old");
	}

	@PostMapping("createWechatS")
	@ResponseBody
	public OrderReturn createWechatS(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "WECHAT", "new");
	}

	@PostMapping("createQqPay")
	@ResponseBody
	public OrderReturn createQqPay(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "QQ", "old");
	}

	@PostMapping("createQqPayS")
	@ResponseBody
	public OrderReturn createQqPayS(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return codePayBusiness.pay(parameter, "QQ", "new");
	}

	@RequestMapping(value = "notify")
	@ResponseBody
	public String notify(HttpServletRequest request) {
		Gson gson = new Gson();
		NotifyDto notifyDto = new NotifyDto();
		String title = request.getParameter("title");
		String remark = request.getParameter("remark");
		if (StringUtils.isNotBlank(title)) {
			notifyDto.mark = request.getParameter("title");
			notifyDto.money = request.getParameter("Money");
			notifyDto.no = request.getParameter("tradeNo");
			LOG.info("个人转账新模式异步回调接收(PC)->" + gson.toJson(notifyDto));
		} else if (StringUtils.isNotBlank(remark)) {
			notifyDto.mark = request.getParameter("remark");
			notifyDto.no = request.getParameter("thirdPartyOrderId");
			notifyDto.money = request.getParameter("money");
		} else {
			notifyDto.mark = request.getParameter("mark");
			notifyDto.money = request.getParameter("money");
			notifyDto.no = request.getParameter("no");
			notifyDto.type = request.getParameter("type");
			LOG.info("个人转账新模式异步回调接收(APP)->" + gson.toJson(notifyDto));
		}
		if (StringUtils.isBlank(notifyDto.mark)) {
			LOG.info("个人转账新模式异步回调接收信息异常");
			return "success";
		}
		try {
			// 利用备注信息找到该订单的回调地址
			String urlNotify = codePayBusiness.getUrl(notifyDto.mark);
			if (StringUtils.isBlank(urlNotify)) {
				LOG.info("【个人转账新模式异步回调接收}】未找到支付回调地址");
				return "success";
			}
			codePayBusiness.sendNotify(notifyDto, urlNotify);
			return "success";
		} catch (JsonSyntaxException e) {
			return "success";
		} catch (Exception e) {
			return "success";
		}
	}

	@PostMapping("notifyDeal")
	@ResponseBody
	public CallbackResult notifyDeal(@RequestBody String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		return codePayBusiness.notify(str);
	}

	public Map<String, String> getUrlParams(String param) {
		Map<String, String> map = new HashMap<String, String>();
		if ("".equals(param) || null == param) {
			return map;
		}
		String[] params = param.split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length == 2) {
				map.put(p[0], p[1]);
			}
		}
		return map;
	}
}
