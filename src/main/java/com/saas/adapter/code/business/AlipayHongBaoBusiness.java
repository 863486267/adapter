package com.saas.adapter.code.business;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.saas.adapter.po.AlipayHongBaoNotify;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.MD5;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;

@Service
public class AlipayHongBaoBusiness {

	@Autowired
	public OrderReturnMain orderReturnMain;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

	@SuppressWarnings("rawtypes")
	public OrderReturn pay(Parameter parameter) {
		String pay_memberid = parameter.merchantChannel.no;// 商户id
		String pay_orderid = parameter.order.no;// 20位订单号 时间戳+6位随机字符串组成
		String pay_bankcode = "919";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String pay_applydate = sdf.format(new Date());// yyyy-MM-dd HH:mm:ss
		String pay_notifyurl = parameter.order.orderConfig.notifyUrl;// 通知地址
		String pay_callbackurl = parameter.order.orderConfig.successUrl;// 回调地址
		String pay_amount = "";
		try {
			pay_amount = changeF2Y(parameter.order.money);
		} catch (Exception e1) {
			e1.printStackTrace();
			return orderReturnMain.failReturn(e1.getLocalizedMessage());
		}
		String subject = "";
		if (parameter.order.detail != null) {
			subject = parameter.order.detail;
		} else if (parameter.order.body != null) {
			subject = parameter.order.body;
		} else {
			subject = "充值";
		}
		String pay_productname = subject;
		String stringSignTemp = "pay_amount=" + pay_amount + "&pay_applydate=" + pay_applydate + "&pay_bankcode="
				+ pay_bankcode + "&pay_callbackurl=" + pay_callbackurl + "&pay_memberid=" + pay_memberid
				+ "&pay_notifyurl=" + pay_notifyurl + "&pay_orderid=" + pay_orderid;
		String pay_md5sign = "";
		try {
			pay_md5sign = MD5.sign(stringSignTemp, "&key=" + parameter.merchantChannel.key, "UTF-8").toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getLocalizedMessage());
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("pay_amount", pay_amount);
		params.add("pay_applydate", pay_applydate);
		params.add("pay_bankcode", pay_bankcode);
		params.add("pay_callbackurl", pay_callbackurl);
		params.add("pay_memberid", pay_memberid);
		params.add("pay_notifyurl", pay_notifyurl);
		params.add("pay_orderid", pay_orderid);
		params.add("pay_productname", pay_productname);
		params.add("pay_md5sign", pay_md5sign);
		HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(params, headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange("http://anqupay-1788.net/Pay_Index.html", HttpMethod.POST, requestEntity,
					String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(response == null) {
			return orderReturnMain.failReturn("上游返回空");
		}
		String str = response.toString();
		String aa = str.substring(str.indexOf("Location=[")+10, str.lastIndexOf("]"));
		return orderReturnMain.successReturn(aa);
	}
	
	
	public CallbackResult notify(String str) {
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		String params = String.valueOf(paramMap.get("params"));
		AlipayHongBaoNotify alipayHongBaoNotify = gson.fromJson(params, AlipayHongBaoNotify.class);
		if("00".equals(alipayHongBaoNotify.returncode.get(0))) {
			return saasNotifyParams.successParams(alipayHongBaoNotify.transaction_id.get(0), "OK");
		}
		return null;
	}
	

	private String changeF2Y(String amount) throws Exception {
		if (!amount.matches("\\-?[0-9]+")) {
			throw new Exception("分转元");
		}
		return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(100)).toString();
	}




}
