package com.saas.adapter.code.business;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.request.AlipayTradeOrderSettleRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradeOrderSettleResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.MerchantChannelAptitude;
import com.saas.adapter.po.OrderParams;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;

@Service
public class AlipayCreateBusiness {

	private static Logger LOG = LoggerFactory.getLogger(AlipayCreateBusiness.class);

	@Autowired
	public OrderReturnMain orderReturnMain;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

	@Autowired
	private TokenClient tokenClient;

	@Autowired
	private RestTemplate restTemplate;

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	private static final String PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCmtiVvmS4o683EqMKgIaFNtVQTlYQzjgnJQukPlW8nEoLd0s30Wy2i4wnduSMc0ezvWxWHx8GB5ZxY1q2LElIcJ8V2WiNl7LLM/g7Wi4cec9JL/CrY/rrHS0kxcZp8gx+cj8p6g1+ygMLDurmbnlgymR3ywr8J3XhLmImH/76/vkGoeIU/liPBjhYA+LthdP/PWzN8aKXPbTlxkQTX1W23UyGfpwaIbQgL+tCcIrx9mmlHOOgcat6VWeD835oLC0mWstkwBO/5U1dwGds4o/cL0WohkPF5TpYahVUmOd7CiYyyQ5poa7bESj7InMCGNyrzS6Bsvj52x8fEyCsRHhLBAgMBAAECggEAJoh7ZqwVS0gVq5sGQVUEn52F7XDlubTe9jINBJkZtAJHVVZgXFam2bt4kFM2lna/OSPoehXl2ExmMvKL8lUtvWxD6/Mxs2FegptGeriNZrCGryF+FeHq8h2osVD80ELOj7V8q4yqFexFGFCUji+vWyKhFXeGCJW0NGTxhQnL4S2M2joGm2DUF9nZT/kMEeiQfEd/IiiZGWxGt/BthAgh9O0TRfCry2c7/3jKYVze/e8KqeGEf4J4ocoCLgOWQixaBUH6LyRwA1Cr0RPhGQB2h2kq8ykieL/YWQdMukT1XZpyrtQ2tImS/9pHXS21VWvQ9t+fEdi70BD87kCFfmKycQKBgQDfp3vLZQd0644q3MkvQTKcYpmTAaqkdceGsdugcUzqz3liL8zwzOv4c/0Y6lW60zBm5JUSNSArmfzELDT31nxcBOaW8HqXxfP8lLHPYDMc69DTngQyWEktZ2YqJlmmXDvg4URGaToapyqyyPc7VksoRCxm57kxlXpUTnzPIUT1WwKBgQC+0m4TmjZFgQ/I57Y0iNNIGa04wOpLrPlzrLo8GnSryRvsrYD1jcFtCdDM0RyzVn0P9Q7OP1LxpHQY04sgp5Gg866o2HlkK4T0e0jp5UImCqVbJ+IY7+eUOBimEm/xgwlSssyI6ipibVeq9+wNR7KT+lm7sRkZcKr9/Jckhg0nEwKBgGakWbcNxa9OD6/yJc6vEEgfJDYIVMUnXIufgpkOhmtZDm8WWurEUSN0L9rIaNHV7Ge8TgHOKdZ41D0/wIthfy9H/H+XG9wduAzaPQRcNPj37J3B7lEgnWzeAFAly40C1WQuXgQmdMkYoQruGjC8OL/hEbOkcySjYkHgx3zT2u7zAoGBAIw3mnVDAxAtlwi841FVHVPEslmawE9dSmvqh2q00fiNLbzLfZByWqih5tEp85oAOqOoIfoB5BP2EO1RPNGA8GzBwx0E/KZTqD3x/ZEJSoMjnYAPHX/PgW6TjeEyeRmRfR9MjSkqUHzE9f+Gb+kOPzWnZ5qYtuXPuYYqvnEzI6ULAoGBAIu2wfRiXsDYz8kdtfoPG1E4HBl3xaeclQuEFtjwdErvp5aGlMxRIMLm7JBSxvOyqafgrebClHN6HT7l19mVjkXQgRQzcKqZ3X5INapzkm82H6BBl3Nl4djplDEwxgvpwX6AqcSOCWZIoTnqyLZThhaKZ4FdpqcYw3jjcbTtKn+D";

	public OrderReturn payWap(Parameter parameter, String payType, String type) {
		if (parameter.aptitudes.isEmpty() || parameter.aptitudes.size() == 0) {
			return orderReturnMain.failReturn("未配置交易资质参数");
		}
		// 筛选资质
		MerchantChannelAptitude aptitude = selecAptitude(parameter.aptitudes, parameter.order.money, payType);

		if (aptitude == null) {
			return orderReturnMain.failReturn("未找到可用资质参数");
		}

		if (StringUtils.isBlank(parameter.merchantChannel.param)) {
			return orderReturnMain.failReturn("资质参数配置有误");
		}

		try {
			@SuppressWarnings("unchecked")
			Map<String, String> map = JsonUtils.toObject(parameter.merchantChannel.param, Map.class);
			if (!map.containsKey(aptitude.name) || StringUtils.isBlank(aptitude.name)) {
				return orderReturnMain.failReturn("资质数据异常请检查资质");
			}
			if (StringUtils.isBlank(aptitude.domain) && StringUtils.isBlank(aptitude.url)) {
				return orderReturnMain.failReturn("未配置转账账号");
			}
			OrderParams orderParams = new OrderParams();
			String subject = "";
			if (parameter.order.detail != null) {
				subject = parameter.order.detail;
			} else if (parameter.order.body != null) {
				subject = parameter.order.body;
			} else {
				subject = "支付宝支付";
			}
			String monery = changeF2Y(parameter.order.money);
			orderParams.appid = aptitude.account;
			orderParams.privateKey = PRIVATE_KEY;
			orderParams.aptitudeId = aptitude.aptitudeId;
			orderParams.monery = monery;
			orderParams.notifyUrl = parameter.order.orderConfig.notifyUrl;
			orderParams.successUrl = parameter.order.orderConfig.successUrl;
			orderParams.subject = subject;
			orderParams.no = parameter.order.no;
			orderParams.publicKey = map.get(aptitude.name);
			aptitude.aliPublicKey = map.get(aptitude.name);
			// 未支付条数累计
			tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
			tokenClient.savePayQualification(parameter.order.id, JsonUtils.toJson(aptitude));
			tokenClient.savePayUrl(parameter.order.id, JsonUtils.toJson(orderParams),5);
			return orderReturnMain.successReturn(aptitude.domain + "/transitNew/payWap/" + parameter.order.id,
					"资质账号:" + aptitude.account, parameter.order.money);// 返回给商户支付链接);
		} catch (Exception e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getMessage());
		}
	}

	public OrderReturn pay(Parameter parameter, String payType, String type) {

		if (parameter.aptitudes.isEmpty() || parameter.aptitudes.size() == 0) {
			return orderReturnMain.failReturn("未配置交易资质参数");
		}
		// 筛选资质
		MerchantChannelAptitude aptitude = selecAptitude(parameter.aptitudes, parameter.order.money, payType);

		if (aptitude == null) {
			return orderReturnMain.failReturn("未找到可用资质参数");
		}

		if (StringUtils.isBlank(parameter.merchantChannel.param)) {
			return orderReturnMain.failReturn("资质参数配置有误");
		}

		try {
			@SuppressWarnings("unchecked")
			Map<String, String> map = JsonUtils.toObject(parameter.merchantChannel.param, Map.class);
			if (!map.containsKey(aptitude.name) || StringUtils.isBlank(aptitude.name)) {
				return orderReturnMain.failReturn("资质数据异常请检查资质");
			}
			if (StringUtils.isBlank(aptitude.domain) && StringUtils.isBlank(aptitude.url)) {
				return orderReturnMain.failReturn("未配置分账账号");
			}

			String monery = changeF2Y(parameter.order.money);
			AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
					aptitude.account, PRIVATE_KEY, "json", "utf-8", map.get(aptitude.name), "RSA2");
			AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
			request.setNotifyUrl(parameter.order.orderConfig.notifyUrl);
			Map<String, String> mapAli = new HashMap<>();
			String subject = "";
			if (parameter.order.detail != null) {
				subject = parameter.order.detail;
			} else if (parameter.order.body != null) {
				subject = parameter.order.body;
			} else {
				subject = "支付宝支付";
			}
			mapAli.put("total_amount", monery);
			mapAli.put("subject", subject);
			mapAli.put("enable_pay_channels", "balance,moneyFund,debitCardExpress");// 可用渠道
			mapAli.put("timeout_express", "30m");// 订单过期时间
			mapAli.put("out_trade_no", parameter.order.no);
			request.setBizContent((new Gson()).toJson(mapAli));
			AlipayTradePrecreateResponse response = alipayClient.execute(request);
			if (response.isSuccess()) {
				// 未支付条数累计
				tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
				// 通过订单号保存支付资质
				aptitude.aliPublicKey = map.get(aptitude.name);
				tokenClient.savePayQualification(parameter.order.id, JsonUtils.toJson(aptitude));
				if (type.equals("WAP")) {
					boolean savePayParamsTwo = tokenClient.savePayUrl(parameter.order.id, response.getQrCode(),5);// 保存支付地址
					if (!savePayParamsTwo) {
						return orderReturnMain.failReturn("订单预处理失败(00003)");
					}
					return orderReturnMain.successReturn(
							"http://adapter.ivpai.com/transitNew/aliPay/" + parameter.order.id,
							"资质APPID:" + aptitude.account);// 返回给商户支付链接);
				}
				return orderReturnMain.successReturn(response.getQrCode(), "资质APPID:" + aptitude.account);
			}
			return orderReturnMain.failReturn(response.getSubMsg());
		} catch (JsonParseException e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getMessage());
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked" })
	public CallbackResult notifyWap(String str) {
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
		Map<?, ?> params = (Map<?, ?>) paramMap.get("params");
		String money = String.valueOf(orderMap.get("money"));
		String orderId = String.valueOf(orderMap.get("id"));
		ArrayList<String> tradeStatusList = (ArrayList<String>) params.get("trade_status");// TRADE_SUCCESS
		ArrayList<String> moneyList = (ArrayList<String>) params.get("total_amount");
		ArrayList<String> tradeNoList = (ArrayList<String>) params.get("trade_no");
		String payMonery = yuan2FenInt(moneyList.get(0));
		String monery = money.substring(0, money.indexOf("."));
		if ("TRADE_SUCCESS".equals(tradeStatusList.get(0))) {
			if (monery.equals(payMonery)) {
				String strOne = tokenClient.getQualification(orderId);
				if (StringUtils.isNotBlank(strOne)) {
					MerchantChannelAptitude aptitude = JsonUtils.toObject(strOne, MerchantChannelAptitude.class);
					// 累加交易金额
					tokenClient.setMoney(aptitude.aptitudeId, payMonery);
					// 移除资质为支付累加条数
					tokenClient.removeByNumber(aptitude.aptitudeId);
					// 已支付的订单移除交易支付链接
					tokenClient.remove(orderId);
					// 转账
					AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
							aptitude.account, PRIVATE_KEY, "json", "utf-8", aptitude.aliPublicKey, "RSA2");
					AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
					Map<String, String> mapA = new HashMap<String, String>();
					mapA.put("out_biz_no", tradeNoList.get(0));
					mapA.put("payee_type", "ALIPAY_LOGONID");
					mapA.put("payee_account", aptitude.url);
					mapA.put("amount", moneyList.get(0));
					mapA.put("remark", "转账");
					request.setBizContent(JsonUtils.toJson(mapA));
					AlipayFundTransToaccountTransferResponse response = null;
					try {
						response = alipayClient.execute(request);
						if (response.isSuccess()) {
							LOG.info("转账接口调用成功");
							// 移除订单资质信息
							tokenClient.removeQualificationByOrder(orderId);
						} else {
							LOG.info("转账接口调用异常,异常单号(" + tradeNoList.get(0) + ")");
							LOG.info("Exception information(" + response.getSubMsg() + ")");
							LOG.info("资质关闭(id=" + aptitude.aptitudeId + ")");
							String url = "http://120.78.178.51:15860/aptitude/setAptitudeFalse/" + aptitude.aptitudeId;// 新商宝
							HttpHeaders headers = new HttpHeaders();
							headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
							HttpEntity<String> entity = new HttpEntity<String>(headers);
							this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
							LOG.info("资质关闭close((" + JsonUtils.toJson(aptitude) + ")");
						}
						return saasNotifyParams.successParams(tradeNoList.get(0), "success");
					} catch (AlipayApiException e) {
						e.printStackTrace();
						if (e.getMessage().indexOf("sign") != -1) {
							return saasNotifyParams.successParams(tradeNoList.get(0), "success");
						}
						LOG.info("转账接口调用异常,异常单号(" + tradeNoList.get(0) + ")");
						LOG.info("资质关闭(id=" + aptitude.aptitudeId + ")");
						String url = "http://120.78.178.51:15860/aptitude/setAptitudeFalse/" + aptitude.aptitudeId;// 新商宝
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
						HttpEntity<String> entity = new HttpEntity<String>(headers);
						this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
						LOG.info("资质关闭close((" + JsonUtils.toJson(aptitude) + ")");
						return saasNotifyParams.successParams(tradeNoList.get(0), "success");
					}
				}
				return saasNotifyParams.successParams(tradeNoList.get(0), "success");
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public CallbackResult notify(String str) {
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
		Map<?, ?> params = (Map<?, ?>) paramMap.get("params");
		String money = String.valueOf(orderMap.get("money"));
		String orderId = String.valueOf(orderMap.get("id"));
		ArrayList<String> tradeStatusList = (ArrayList<String>) params.get("trade_status");// TRADE_SUCCESS
		ArrayList<String> moneyList = (ArrayList<String>) params.get("total_amount");
		ArrayList<String> tradeNoList = (ArrayList<String>) params.get("trade_no");
		String payMonery = yuan2FenInt(moneyList.get(0));
		String monery = money.substring(0, money.indexOf("."));
		if ("TRADE_SUCCESS".equals(tradeStatusList.get(0))) {
			if (monery.equals(payMonery)) {
				threadPool.submit(() -> {
					String strOne = tokenClient.getQualification(orderId);
					if (StringUtils.isNotBlank(strOne)) {
						MerchantChannelAptitude aptitude = JsonUtils.toObject(strOne, MerchantChannelAptitude.class);
						// 累加交易金额
						tokenClient.setMoney(aptitude.aptitudeId, payMonery);
						// 移除资质为支付累加条数
						tokenClient.removeByNumber(aptitude.aptitudeId);
						// 已支付的订单移除交易支付链接
						tokenClient.remove(orderId);
						// 分账
						String transIn = "";
						if (StringUtils.isNotBlank(aptitude.domain) && StringUtils.isNotBlank(aptitude.url)) {
							int index = (int) (Math.random() * 2);
							if (index == 0) {
								transIn = aptitude.domain;
							} else {
								transIn = aptitude.url;
							}
						} else if (StringUtils.isNotBlank(aptitude.domain)) {
							transIn = aptitude.domain;
						} else {
							transIn = aptitude.url;
						}
						AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
								aptitude.account, PRIVATE_KEY, "json", "utf-8", aptitude.aliPublicKey, "RSA2");
						AlipayTradeOrderSettleRequest request = new AlipayTradeOrderSettleRequest();
						request.setBizContent("{" + "\"out_request_no\":\"" + tradeNoList.get(0) + "\","
								+ "\"trade_no\":\"" + tradeNoList.get(0) + "\"," + "      \"royalty_parameters\":[{"
								+ "\"trans_in\":\"" + transIn + "\"," + "\"amount\":" + moneyList.get(0) + ","
								+ "\"amount_percentage\":100," + "\"desc\":\"分账\"" + "        }]" + "  }");
						AlipayTradeOrderSettleResponse response;
						try {
							response = alipayClient.execute(request);
							if (response.isSuccess()) {
								if ("10000".equals(response.getCode())) {
									LOG.info("分账接口调用成功");
									// 移除订单资质信息
									tokenClient.removeQualificationByOrder(orderId);
								} else {
									LOG.info("分账接口调用异常,异常单号(" + tradeNoList.get(0) + ")");
									LOG.info("Exception information(" + response.getSubMsg() + ")");
									LOG.info("资质关闭(id=" + aptitude.aptitudeId + ")");
									String url = "http://120.78.178.51:15860/aptitude/setAptitudeFalse/"
											+ aptitude.aptitudeId;// 新商宝
									HttpHeaders headers = new HttpHeaders();
									headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
									HttpEntity<String> entity = new HttpEntity<String>(headers);
									this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
									LOG.info("资质关闭close((" + JsonUtils.toJson(aptitude) + ")");
								}
							} else {
								LOG.info("调用失败");
								LOG.info("Call fails:" + response.getSubMsg() + "," + response.getSubCode());
								LOG.info("资质关闭(id=" + aptitude.aptitudeId + ")");
								String url = "http://120.78.178.51:15860/aptitude/setAptitudeFalse/"
										+ aptitude.aptitudeId;// 新商宝
								HttpHeaders headers = new HttpHeaders();
								headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
								HttpEntity<String> entity = new HttpEntity<String>(headers);
								this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
								LOG.info("资质关闭close((" + JsonUtils.toJson(aptitude) + ")");
							}
						} catch (AlipayApiException e) {
							e.printStackTrace();
							LOG.info("分账接口调用异常");
							LOG.info("资质关闭(id=" + aptitude.aptitudeId + ")");
							String url = "http://120.78.178.51:15860/aptitude/setAptitudeFalse/" + aptitude.aptitudeId;// 新商宝
							HttpHeaders headers = new HttpHeaders();
							headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
							HttpEntity<String> entity = new HttpEntity<String>(headers);
							this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
							LOG.info("资质关闭close((" + JsonUtils.toJson(aptitude) + ")");
						}
					}
				});
				return saasNotifyParams.successParams(tradeNoList.get(0), "success");
			}
		}
		return null;
	}

	/**
	 * 将元为单位的参数转换为分 , 只对小数点前2位支持
	 *
	 * @param yuan
	 * @return
	 * @throws Exception
	 */
	private String yuan2FenInt(String yuan) {
		BigDecimal fenBd = new BigDecimal(yuan).multiply(new BigDecimal(100));
		fenBd = fenBd.setScale(0, BigDecimal.ROUND_HALF_UP);
		return fenBd.toString();
	}

	private MerchantChannelAptitude selecAptitude(List<MerchantChannelAptitude> merchantChannelAptitude, String money,
			String payType) {

		List<MerchantChannelAptitude> screening = new ArrayList<MerchantChannelAptitude>();

		for (MerchantChannelAptitude MerchantChannelAptitude : merchantChannelAptitude) {
			if (!MerchantChannelAptitude.enabled) {
				// 排除未激活的资质
				continue;
			}
			if (!payType.equals(MerchantChannelAptitude.type.toString())) {
				// 排除不符合交易类型的资质
				continue;
			}
			if (MerchantChannelAptitude.dayMax == 0) {
				// 未设置每日交易金额的资质直接添加
				screening.add(MerchantChannelAptitude);
				continue;
			}
			Long moneying = tokenClient.getPayMongIng(MerchantChannelAptitude.aptitudeId);
			if (MerchantChannelAptitude.dayMax - moneying - Long.valueOf(money) < 0) {
				// 排除超出日限交易的资质
				LOG.info(
						"资质名称:" + MerchantChannelAptitude.name + ",资质账号:" + MerchantChannelAptitude.account + " 额度已上限");
				continue;
			}
			screening.add(MerchantChannelAptitude);
		}
		if (screening == null || screening.size() == 0) {
			LOG.info("没有符合条件的资质");
			return null;
		}
		Gson gson = new Gson();
		int index = (int) (Math.random() * screening.size());
		try {
			// 从符合条件的集合中随机挑选一组资质
			MerchantChannelAptitude arbitration = gson.fromJson(gson.toJson(screening.get(index)),
					MerchantChannelAptitude.class);
			return arbitration;
		} catch (JsonSyntaxException e) {
			LOG.info("JSON格式转化错误--->" + e.getMessage());
			return null;
		}
	}

	/**
	 * 分转元
	 *
	 * @param amount
	 * @return
	 * @throws Exception
	 */
	private String changeF2Y(String amount) throws Exception {
		if (!amount.matches("\\-?[0-9]+")) {
			throw new Exception("分转元");
		}
		return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(100)).toString();
	}

}
