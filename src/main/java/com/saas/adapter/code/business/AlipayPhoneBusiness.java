package com.saas.adapter.code.business;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.po.HuaBuDianParams;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.MD5;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.StringSort;

@Service
public class AlipayPhoneBusiness {

	private static Logger LOG = LoggerFactory.getLogger(AlipayPhoneBusiness.class);

	@Autowired
	public OrderReturnMain orderReturnMain;

	@Autowired
	private TokenClient tokenClient;

	/**
	 * 统一下单
	 * 
	 * @throws Exception
	 */
	public OrderReturn payWap(Parameter parameter, String payType) {
		Map<String, String> map = new HashMap<String, String>();
		StringSort stringSort = new StringSort();
		map.put("mch", parameter.merchantChannel.no);
		if ("aliqrcode".equals(payType)) {
			map.put("pay_type", payType);
		}
		if ("aliwap".equals(payType)) {
			map.put("pay_type", payType);
		}
		if ("alih5".equals(payType)) {
			map.put("pay_type", payType);
		}

		map.put("money", parameter.order.money);
		String time = String.valueOf(Math.round(new Date().getTime() / 1000));
		map.put("time", time);
		map.put("order_id", parameter.order.no);//
		map.put("return_url", parameter.order.orderConfig.successUrl);
		map.put("notify_url", "http://adapter.ivpai.com/huaBuDianController/notify");
		tokenClient.savePayParams(parameter.order.id.replaceAll("-", ""), parameter.order.orderConfig.notifyUrl);
		map.put("extra", parameter.order.id.replaceAll("-", ""));//
		map.put("longtitude", "36");//
		map.put("latitude", "18");//
		map.put("deviceinfo", parameter.order.no);//
		String str = parameter.order.no + parameter.order.money + payType + time + parameter.merchantChannel.no;
		String sign = "";
		try {
			sign = MD5.sign(str, MD5.sign(parameter.merchantChannel.key, "", "UTF-8").toLowerCase(), "UTF-8")
					.toLowerCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("sign", sign);
		String req = stringSort.getUrlParamsByMap(map, true);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		String returnParams = restTemplate
				.exchange("http://api.oba88.cn/waporder/order_add?" + req, HttpMethod.GET, entity, String.class)
				.getBody();
		HuaBuDianParams huaBuDianParams = (new Gson()).fromJson(returnParams, HuaBuDianParams.class);
		if (huaBuDianParams.net && huaBuDianParams.ok) {
			return orderReturnMain.successReturn(huaBuDianParams.data, parameter.merchantChannel.no,
					parameter.order.money);//
		}

		return orderReturnMain.failReturn(returnParams);
	}

	public static Integer StringToTimestamp(String time) {

		int times = 0;
		try {
			times = (int) ((Timestamp.valueOf(time).getTime()) / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (times == 0) {
			LOG.info("String转10位时间戳失败");
		}
		return times;

	}

}
