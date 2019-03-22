package com.saas.adapter.code.controllers;

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
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.business.AlipayRedEnvelope;
import com.saas.adapter.code.dto.AlipayRedEnvelopeNotifyDto;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.MerchantChannelAptitude;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.SaasNotifyParams;
import com.saas.adapter.tools.StringSort;

@Controller
@RequestMapping("alipayRedEnvelope")
public class AlipayRedEnvelopeController {

	private static Logger LOG = LoggerFactory.getLogger(AlipayRedEnvelopeController.class);

	@Autowired
	private AlipayRedEnvelope aAlipayRedEnvelope;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

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
		return aAlipayRedEnvelope.pay(parameter , "WAP");
	}
	
	/**
	 * 支付宝下单(scandCode)
	 */
	@PostMapping("scandCode")
	@ResponseBody
	public OrderReturn scandCode(@RequestBody Parameter parameter) {
		if (parameter == null) {
			OrderReturn orderReturn = new OrderReturn();
			orderReturn.success = false;
			orderReturn.resultMessage = "请求参数为空";
			return orderReturn;
		}
		return aAlipayRedEnvelope.pay(parameter , "SCANDCODE");
	}

	@RequestMapping(value = "notify")
	@ResponseBody
	public String notify(HttpServletRequest request) {
		String mark = request.getParameter("mark");
		String money = request.getParameter("money");
		String no = request.getParameter("no");
		LOG.info("mark:" + mark );
		LOG.info("money:" + money);
		LOG.info("no:" + no);
		if (StringUtils.isBlank(mark) || StringUtils.isBlank(money)) {
			return "fail";
		}
		String notifyUrl = tokenClient.getPayParams(mark);
		if (StringUtils.isBlank(notifyUrl)) {
			return "fail";
		}
		StringSort stringSort = new StringSort();
		AlipayRedEnvelopeNotifyDto alipayRedEnvelopeNotifyDto = new AlipayRedEnvelopeNotifyDto();
		alipayRedEnvelopeNotifyDto.money = stringSort.yuan2FenInt(money);
		alipayRedEnvelopeNotifyDto.no = no;
		alipayRedEnvelopeNotifyDto.mark = mark;
		restTemplate.postForObject(notifyUrl, alipayRedEnvelopeNotifyDto, String.class);
		return "success";
	}

	@RequestMapping(value = "notifyDeal")
	@ResponseBody
	public CallbackResult notifyDeal(@RequestBody String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
		String money = String.valueOf(orderMap.get("money"));
		String body = String.valueOf(paramMap.get("body"));
		AlipayRedEnvelopeNotifyDto alipayRedEnvelopeNotifyDto = gson.fromJson(body, AlipayRedEnvelopeNotifyDto.class);
		String nowMoney = String.valueOf(alipayRedEnvelopeNotifyDto.money);
		String no = String.valueOf(alipayRedEnvelopeNotifyDto.no);
		String mark = String.valueOf(alipayRedEnvelopeNotifyDto.mark);

		if (!nowMoney.equals(money.substring(0, money.indexOf(".")))) {
			LOG.info("订单金额不正确");
			return saasNotifyParams.errorParams("订单金额不正确", "ok");
		}
		String strOne = tokenClient.getQualification(mark);
		if (StringUtils.isNotBlank(strOne)) {
			MerchantChannelAptitude aptitude = JsonUtils.toObject(strOne, MerchantChannelAptitude.class);
			// 累加交易金额
			tokenClient.setMoney(aptitude.aptitudeId, nowMoney);
			// 移除订单资质信息
			tokenClient.removeQualificationByOrder(mark);
			// 移除资质为支付累加条数
			tokenClient.removeByNumber(aptitude.aptitudeId);
			// 已支付的订单移除交易支付链接
			tokenClient.remove(mark);
			tokenClient.remove(orderMap.get("id").toString());
			tokenClient.remove("A"+orderMap.get("id").toString());
		}
		return saasNotifyParams.successParams(no, "success");
	}

}
