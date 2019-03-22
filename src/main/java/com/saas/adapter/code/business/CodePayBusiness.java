package com.saas.adapter.code.business;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.dto.FeedBack;
import com.saas.adapter.code.dto.NewFeedBack;
import com.saas.adapter.code.dto.NotifyDto;
import com.saas.adapter.code.dto.RequestParams;
import com.saas.adapter.code.dto.UpstreamAReturn;
import com.saas.adapter.interfaces.PayBusiness;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.MerchantChannelAptitude;
import com.saas.adapter.po.Order;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.MD5;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;

@Component
public class CodePayBusiness implements PayBusiness {

	private static Logger LOG = LoggerFactory.getLogger(CodePayBusiness.class);

	@Autowired
	public OrderReturnMain orderReturnMain;

	@Autowired
	public SaasNotifyParams saasNotifyParams;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private TokenClient tokenClient;

	private ExecutorService threadPool = Executors.newCachedThreadPool();

	public OrderReturn pay(Parameter parameter, String payType, String type) {

		if (parameter.aptitudes.isEmpty() || parameter.aptitudes.size() == 0) {
			return orderReturnMain.failReturn("未配置交易资质参数");
		}
		// 筛选资质
		MerchantChannelAptitude aptitude = selecAptitude(parameter.aptitudes, parameter.order.money, payType);

		if (aptitude == null) {
			return orderReturnMain.failReturn("未找到可用资质参数");
		}
		/////////////////////////////////////////////
//		String notifyUrl = "";
//		if (parameter.order.orderConfig.notifyUrl.indexOf("api.changchunjlqcdj.com") != -1) {
//			notifyUrl = "http://lmapi.changchunjlqcdj.com/open/v1/order/callback/" + parameter.order.orderConfig.notifyUrl
//					.substring(parameter.order.orderConfig.notifyUrl.indexOf("callback/") + 9,
//							parameter.order.orderConfig.notifyUrl.length());
//		} else {
//			notifyUrl = parameter.order.orderConfig.notifyUrl;
//		} // 联梦
//			/////////////////////////////////////////////
		String notifyUrl = parameter.order.orderConfig.notifyUrl;// 商宝
		RequestParams requestParams = new RequestParams();
		try {
			requestParams.money = changeF2Y(parameter.order.money);
			requestParams.mark = getId(payType);
			if ("ALIPAY".equals(payType)) {
				requestParams.type = "alipay";
			}
			if ("WECHAT".equals(payType)) {
				requestParams.type = "wechat";
			}
			if ("QQ".equals(payType)) {
				requestParams.type = "qq";
			}
			String url = "";
			if ("old".equals(type)) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				HttpEntity<String> entity = new HttpEntity<String>(headers);
				String uri = aptitude.url + "/getpay?money=" + requestParams.money + "&mark=" + requestParams.mark
						+ "&type=" + requestParams.type;
				LOG.info("自动生码下单参数(老版本)-->" + uri);
				String strbody = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();
				LOG.info("自动生码返回参数(老版本)-->" + strbody);
				if (StringUtils.isBlank(strbody)) {
					threadPool.submit(() -> {
						tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
					});
					return orderReturnMain.failReturn("下单为空");
				}
				FeedBack feedBack = JSONObject.parseObject(strbody, FeedBack.class);
				if (StringUtils.isBlank(feedBack.payurl)) {
					threadPool.submit(() -> {
						tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
					});
					return orderReturnMain.failReturn("下单失败");
				}
				if ("WECHAT".equals(payType)) {
					url = feedBack.payurl;
				} else {
					url = aptitude.domain + "/transitNew/pay/" + parameter.order.id;// 返回给商户支付链接
					// 利用订单信息保存相关支付链接
					boolean savePayParamsTwo = tokenClient.savePayUrl(parameter.order.id, feedBack.payurl,5);// 保存支付地址
					if (!savePayParamsTwo) {
						return orderReturnMain.failReturn("订单预处理失败(00003)");
					}
				}
			}
			if ("new".equals(type)) {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				HttpEntity<String> entity = new HttpEntity<String>(headers);
				String sign = MD5.sign("12345678" + requestParams.money + requestParams.type + requestParams.mark,
						"12345678", "utf-8");
				String strUrl = "http://47.106.223.127:8099/getpay?money=" + requestParams.money + "&mark="
						+ requestParams.mark + "&type=" + requestParams.type + "&userid=" + aptitude.url + "&sign="
						+ sign;
				LOG.info("自动生码下单参数(新版本)-->" + strUrl);
				String strbody = restTemplate.exchange(strUrl, HttpMethod.GET, entity, String.class).getBody();
				LOG.info("自动生码返回参数(新版本)-->" + strbody);
				if (StringUtils.isBlank(strbody)) {
					threadPool.submit(() -> {
						tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
					});
					return orderReturnMain.failReturn("下单为空");
				}
				NewFeedBack newFeedBack = JSONObject.parseObject(strbody, NewFeedBack.class);
				if (!"1".equals(newFeedBack.code)) {
					threadPool.submit(() -> {
						tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
					});
					return orderReturnMain.failReturn(newFeedBack.msg);
				}

				if ("WECHAT".equals(payType)) {
					// 微信扫码链接不需要进行改动
					url = newFeedBack.payurl;
				} else {
					url = aptitude.domain + "/transitNew/pay/" + parameter.order.id;// 返回给商户支付链接
					// 利用订单信息保存相关支付链接
					boolean savePayParamsTwo = tokenClient.savePayUrl(parameter.order.id, newFeedBack.payurl,5);// 保存支付地址
					if (!savePayParamsTwo) {
						return orderReturnMain.failReturn("订单预处理失败(00003)");
					}
				}
			}

			if ("UpstreamA".equals(type)) {
				if ("wechat".equals(requestParams.type)) {
					requestParams.type = "weixin";
				}
				String strUrl = "userId=" + aptitude.url + "&type=" + requestParams.type + "&money="
						+ requestParams.money + "&remark=" + requestParams.mark + "&thirdPartyOrderId="
						+ parameter.order.no;
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				HttpEntity<String> entity = new HttpEntity<String>(strUrl, headers);
				LOG.info("自动生码下单参数(UpstreamA)-->" + strUrl);
				String strbody = restTemplate.postForObject("http://39.105.80.16:9080/PayHelper/thirdParty/qrcode",
						entity, String.class);
				LOG.info("自动生码返回参数(UpstreamA)-->" + strbody);
				if (StringUtils.isBlank(strbody)) {
					threadPool.submit(() -> {
						tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
					});
					return orderReturnMain.failReturn("下单为空");
				}
				UpstreamAReturn upstreamAReturn = JSONObject.parseObject(strbody, UpstreamAReturn.class);
				if (!"1".equals(upstreamAReturn.code)) {
					threadPool.submit(() -> {
						tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
					});
					return orderReturnMain.failReturn(upstreamAReturn.define);
				}

				if ("WECHAT".equals(payType)) {
					// 微信扫码链接不需要进行改动
					url = upstreamAReturn.payUrl;
				} else {
					url = aptitude.domain + "/transitNew/pay/" + parameter.order.id;// 返回给商户支付链接
					// 利用订单信息保存相关支付链接
					boolean savePayParamsTwo = tokenClient.savePayUrl(parameter.order.id, upstreamAReturn.payUrl,5);// 保存支付地址
					if (!savePayParamsTwo) {
						return orderReturnMain.failReturn("订单预处理失败(00003)");
					}
				}
			}
			
			if ("newPay".equals(type)) {
				String str = "alipays://platformapi/startapp?appId=09999988&actionType=toAccount&goBack=NO&userId="+aptitude.name+"&amount="+requestParams.money+"&memo="+requestParams.mark;
				url = aptitude.domain + "/transitNew/payNew/" + parameter.order.id;// 返回给商户支付链接
				// 利用订单信息保存相关支付链接
				boolean savePayParamsTwo = tokenClient.savePayUrl(parameter.order.id, URLEncoder.encode(str,"utf-8"),5);// 保存支付地址
				if (!savePayParamsTwo) {
					return orderReturnMain.failReturn("订单预处理失败(00003)");
				}
			}

			// 利用订单备注信息保存支付回调地址
			boolean savePayParams = tokenClient.savePayParams(requestParams.mark, notifyUrl);// 保存回调
			if (!savePayParams) {
				return orderReturnMain.failReturn("订单预处理失败(00001)");
			}
			// 利用订单备注信息保存订单使用资质信息
			boolean savePayQualification = tokenClient.savePayQualification(requestParams.mark,
					JsonUtils.toJson(aptitude));
			if (!savePayQualification) {
				return orderReturnMain.failReturn("订单预处理失败(00002)");
			}
			// 根据资质(ID)累计未支付的条数
			threadPool.submit(() -> {
				tokenClient.addNumber(aptitude.aptitudeId, aptitude.count);
			});
			return orderReturnMain.successReturn(url, "(" + aptitude.name + ")" + requestParams.mark);
		} catch (RestClientException e) {
			e.printStackTrace();
			return orderReturnMain.failReturn(e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return orderReturnMain.failReturn("金额转化异常");
		}
	}

	@Override
	public CallbackResult notify(String str) {
		Gson gson = new Gson();
		Map<?, ?> map = gson.fromJson(str, Map.class);
		Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
		Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
		String money = String.valueOf(orderMap.get("money"));
		String body = String.valueOf(paramMap.get("body"));
		NotifyDto notifyDto = gson.fromJson(body, NotifyDto.class);
		String nowMoney = yuan2FenInt(notifyDto.money);
		if (!nowMoney.equals(money.substring(0, money.indexOf(".")))) {
			LOG.info("订单金额不正确");
			return saasNotifyParams.errorParams("订单金额不正确", "success");
		}

		threadPool.submit(() -> {
			String strOne = tokenClient.getQualification(notifyDto.mark);
			if (StringUtils.isNotBlank(strOne)) {
				MerchantChannelAptitude aptitude = JsonUtils.toObject(strOne, MerchantChannelAptitude.class);
				// 累加交易金额
				tokenClient.setMoney(aptitude.aptitudeId, nowMoney);
				// 移除订单资质信息
				tokenClient.removeQualificationByOrder(notifyDto.mark);
				// 移除资质为支付累加条数
				tokenClient.removeByNumber(aptitude.aptitudeId);
				// 已支付的订单移除交易支付链接
				tokenClient.remove(orderMap.get("id").toString());
			}
		});
		return saasNotifyParams.successParams(notifyDto.no, "success");
	}

	@Override
	public CallbackResult query(Order order) {
		return null;
	}

	/**
	 * 获取订单回调地址
	 * 
	 * @param id
	 * @return
	 */
	public String getUrl(String id) {
		return tokenClient.getPayParams(id);
	}

	/**
	 * 获取订单资质名称
	 * 
	 * @param id
	 * @return
	 */
	public String getQualification(String id) {
		return tokenClient.getQualification(id);
	}

	/**
	 * 回调发送
	 * 
	 * @param notifyDto
	 * @param url
	 */
	public void sendNotify(NotifyDto notifyDto, String url) {

		threadPool.submit(() -> {
			restTemplate.postForObject(url, notifyDto, String.class);
		});
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
	 * 不用接口方法
	 */
	@Override
	public OrderReturn pay(Parameter parameter) {
		return null;
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

	private String getId(String type) {
		int machineId = (int) (Math.random() * 9);
		int hashCodeV = UUID.randomUUID().toString().hashCode();
		if (hashCodeV < 0) {// 有可能是负数
			hashCodeV = -hashCodeV;
		}
		if ("QQ".equals(type)) {
			return machineId + String.format("%011d", hashCodeV);
		}
		return machineId + String.format("%015d", hashCodeV);
	}

	public static <T> List<T> getObjectList(String jsonString, Class<T> cls) {
		List<T> list = new ArrayList<T>();
		try {
			Gson gson = new Gson();
			JsonArray arry = new JsonParser().parse(jsonString).getAsJsonArray();
			for (JsonElement jsonElement : arry) {
				list.add(gson.fromJson(jsonElement, cls));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
