package com.saas.adapter.code.controllers;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayOpenAuthTokenAppRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayOpenAuthTokenAppResponse;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.dto.ShunLongParams;
import com.saas.adapter.po.OrderParams;
import com.saas.adapter.tools.JsonUtils;
import com.saas.adapter.tools.StringSort;

@Controller
@RequestMapping("transitNew")
public class CodeJumpPay {

	@Autowired
	private TokenClient tokenClient;

	private static Logger LOG = LoggerFactory.getLogger(CodeJumpPay.class);

	@GetMapping("payWap/{id}")
	@ResponseBody
	public String payWap(@PathVariable String id, ModelMap model) {

		String paramJson = tokenClient.getPayParams(id);
		if (StringUtils.isBlank(paramJson)) {
			return "订单异常请重新下单";
		}
		LOG.info("支付宝WAP下单参数:" + paramJson);
		OrderParams orderParams = JsonUtils.toObject(paramJson, OrderParams.class);
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", orderParams.appid,
				orderParams.privateKey, "json", "utf-8", orderParams.publicKey, "RSA2");
		AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
		request.setNotifyUrl(orderParams.notifyUrl);
		request.setReturnUrl(orderParams.successUrl);
		Map<String, String> mapAli = new HashMap<>();
		mapAli.put("total_amount", orderParams.monery);
		mapAli.put("subject", orderParams.subject);
		mapAli.put("enable_pay_channels", "balance,moneyFund,debitCardExpress");// 可用渠道
		mapAli.put("timeout_express", "30m");// 订单过期时间
		mapAli.put("out_trade_no", orderParams.no);
		mapAli.put("quit_url", orderParams.successUrl);
		request.setBizContent((new Gson()).toJson(mapAli));
		try {
			return alipayClient.pageExecute(request).getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return e.getErrCode();
		}
	}

	@RequestMapping(value = "getUserId")
	@ResponseBody
	public Object notify(HttpServletRequest requestR) {
		String appAuthCode = requestR.getParameter("app_auth_code");
		LOG.info("支付宝授权获取__" + appAuthCode);
		if (StringUtils.isBlank(appAuthCode)) {
			return "获取USERID失败";
		}
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", "2016112603359618",
				"MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCmtiVvmS4o683EqMKgIaFNtVQTlYQzjgnJQukPlW8nEoLd0s30Wy2i4wnduSMc0ezvWxWHx8GB5ZxY1q2LElIcJ8V2WiNl7LLM/g7Wi4cec9JL/CrY/rrHS0kxcZp8gx+cj8p6g1+ygMLDurmbnlgymR3ywr8J3XhLmImH/76/vkGoeIU/liPBjhYA+LthdP/PWzN8aKXPbTlxkQTX1W23UyGfpwaIbQgL+tCcIrx9mmlHOOgcat6VWeD835oLC0mWstkwBO/5U1dwGds4o/cL0WohkPF5TpYahVUmOd7CiYyyQ5poa7bESj7InMCGNyrzS6Bsvj52x8fEyCsRHhLBAgMBAAECggEAJoh7ZqwVS0gVq5sGQVUEn52F7XDlubTe9jINBJkZtAJHVVZgXFam2bt4kFM2lna/OSPoehXl2ExmMvKL8lUtvWxD6/Mxs2FegptGeriNZrCGryF+FeHq8h2osVD80ELOj7V8q4yqFexFGFCUji+vWyKhFXeGCJW0NGTxhQnL4S2M2joGm2DUF9nZT/kMEeiQfEd/IiiZGWxGt/BthAgh9O0TRfCry2c7/3jKYVze/e8KqeGEf4J4ocoCLgOWQixaBUH6LyRwA1Cr0RPhGQB2h2kq8ykieL/YWQdMukT1XZpyrtQ2tImS/9pHXS21VWvQ9t+fEdi70BD87kCFfmKycQKBgQDfp3vLZQd0644q3MkvQTKcYpmTAaqkdceGsdugcUzqz3liL8zwzOv4c/0Y6lW60zBm5JUSNSArmfzELDT31nxcBOaW8HqXxfP8lLHPYDMc69DTngQyWEktZ2YqJlmmXDvg4URGaToapyqyyPc7VksoRCxm57kxlXpUTnzPIUT1WwKBgQC+0m4TmjZFgQ/I57Y0iNNIGa04wOpLrPlzrLo8GnSryRvsrYD1jcFtCdDM0RyzVn0P9Q7OP1LxpHQY04sgp5Gg866o2HlkK4T0e0jp5UImCqVbJ+IY7+eUOBimEm/xgwlSssyI6ipibVeq9+wNR7KT+lm7sRkZcKr9/Jckhg0nEwKBgGakWbcNxa9OD6/yJc6vEEgfJDYIVMUnXIufgpkOhmtZDm8WWurEUSN0L9rIaNHV7Ge8TgHOKdZ41D0/wIthfy9H/H+XG9wduAzaPQRcNPj37J3B7lEgnWzeAFAly40C1WQuXgQmdMkYoQruGjC8OL/hEbOkcySjYkHgx3zT2u7zAoGBAIw3mnVDAxAtlwi841FVHVPEslmawE9dSmvqh2q00fiNLbzLfZByWqih5tEp85oAOqOoIfoB5BP2EO1RPNGA8GzBwx0E/KZTqD3x/ZEJSoMjnYAPHX/PgW6TjeEyeRmRfR9MjSkqUHzE9f+Gb+kOPzWnZ5qYtuXPuYYqvnEzI6ULAoGBAIu2wfRiXsDYz8kdtfoPG1E4HBl3xaeclQuEFtjwdErvp5aGlMxRIMLm7JBSxvOyqafgrebClHN6HT7l19mVjkXQgRQzcKqZ3X5INapzkm82H6BBl3Nl4djplDEwxgvpwX6AqcSOCWZIoTnqyLZThhaKZ4FdpqcYw3jjcbTtKn+D",
				"json", "UTF-8",
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiCywEg9uV6GF259cnK0sYZqNLuYnbXBG7nbI+iAawsviCfwGPLAVIN2JhuLJpkGR6+KOmsBUVmVdeJykpewccvVLDHJB/yEtG/p41svLYJIhakqbOgj60J6RHAwPgqswfACTU4poSzGhi9wHZAezVrFEdNFEHUq23cfbCfKoNctETwmE8EhNfjuSxW5CfA/uGydRIfAwERbmq4MjdW1MKfn5flhNUGjXfcEZVz3M8F2aLTxGAjMCYoBhIre1z1XO8E4PjnM19DUp7eMByqAylNOglzG3c9s2VR7qSBfGxxuB0K0GVgViT3MeC50OdI5v84AAQRpf3J54nreqHZz+0wIDAQAB",
				"RSA2");
		AlipayOpenAuthTokenAppRequest request = new AlipayOpenAuthTokenAppRequest();
		request.setBizContent("{" + "    \"grant_type\":\"authorization_code\"," + "    \"code\":" + "\"" + appAuthCode
				+ "\"" + "  }");
		try {
			AlipayOpenAuthTokenAppResponse response = alipayClient.execute(request);
			if (response.isSuccess() && "10000".equals(response.getCode())
					&& StringUtils.isNotBlank(response.getAppAuthToken())) {
				return "获取USERID:  " + response.getUserId();
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return "异常:" + e.getLocalizedMessage();
		}
		return "获取失败,请重新尝试或联系管理人员";
	}

	@RequestMapping(value = "payUrl")
	public String payUrl(HttpServletRequest requestR, ModelMap model) {
		String payUrl = requestR.getParameter("payUrl");
		if (StringUtils.isBlank(payUrl)) {
			return "errorTransit";
		}
		model.addAttribute("pGateWayReq", payUrl);
		return "alipayTransit";
	}

	@RequestMapping(value = "platformapi")
	public String platformapi(HttpServletRequest requestR, ModelMap model) throws UnsupportedEncodingException {
		String payUrl = requestR.getParameter("payUrl");
		if (StringUtils.isBlank(payUrl)) {
			return "errorTransit";
		}
		String str = "alipays://platformapi/startapp?appId=20000067&url=" + URLEncoder.encode(payUrl, "UTF-8");
		model.addAttribute("pGateWayReq", str);
		return "alipayTransit";
	}

	@GetMapping("pay/{id}")
	public String getPage(@PathVariable String id, ModelMap model) throws UnsupportedEncodingException {
		String paramJson = tokenClient.getPayParams(id);
		LOG.info("订单:" + id + "实际支付地址=" + paramJson);
		if (StringUtils.isBlank(paramJson)) {
			return "errorTransit";
		}
		String str = "alipays://platformapi/startapp?appId=20000067&url=" + URLEncoder.encode(paramJson, "UTF-8");
		model.addAttribute("pGateWayReq", str);
		return "alipayTransit";
	}

	@GetMapping("aliPay/{id}")
	public String getAliPage(@PathVariable String id, ModelMap model) throws UnsupportedEncodingException {
		String paramJson = tokenClient.getPayParams(id);
		LOG.info("订单:" + id + "实际支付地址=" + paramJson);
		if (StringUtils.isBlank(paramJson)) {
			return "errorTransit";
		}
		String scheme = "alipays://platformapi/startapp?appId=20000067&backBehavior=pop&startMultApp=YES&actionType=WebView&url="
				+ URLEncoder.encode(paramJson, "utf-8");
		model.addAttribute("pGateWayReq", scheme);
		return "alipayTransit";
	}

	@GetMapping("aliPaySecondStep/{id}")
	public String aliPayWapTwo(@PathVariable String id, ModelMap model) throws UnsupportedEncodingException {
		String paramJson = tokenClient.getPayParams("aliPaySecondStep." + id);
		LOG.info(id + " 订单地址:" + paramJson);
		if (StringUtils.isBlank(paramJson)) {
			return "errorTransit";
		}

		model.addAttribute("code", id);
		String url = tokenClient.getPayParams(id);
		String monery = getMonery(url);
		model.addAttribute("monery", "￥ " + monery);
		model.addAttribute("url", url);
		return "alipayWap";
	}

	@GetMapping("aliPaySecondStep/qr/{code}")
	public void qrCode(@PathVariable String code, HttpServletResponse response) {
		String paramJson = tokenClient.getPayParams(code);
		try (OutputStream out = response.getOutputStream()) {
			createQrCode(paramJson, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createQrCode(String content, OutputStream out) throws Exception {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
			}
		}
		ImageIO.write(image, "JPG", out);
	}

	@GetMapping("payNew/{id}")
	public String payNew(@PathVariable String id, ModelMap model) throws UnsupportedEncodingException {
		String paramJson = tokenClient.getPayParams(id);
		LOG.info("订单:" + id + "实际内容=" + paramJson);
		if (StringUtils.isBlank(paramJson)) {
			return "errorTransit";
		}
		model.addAttribute("pGateWayReq", paramJson);
		return "alipayTransit";
	}
	
	@GetMapping("shunLong/{id}")
	public String shunLong(@PathVariable String id, ModelMap model) {
		String paramJson = tokenClient.getPayParams(id);
		LOG.info("订单:" + id + "订单内容=" + paramJson);
		if (StringUtils.isBlank(paramJson)) {
			return "errorTransit";
		}
		ShunLongParams shunLongParams = (new Gson()).fromJson(paramJson, ShunLongParams.class);
		model.addAttribute("pay_memberid", shunLongParams.payMemberid);
		model.addAttribute("pay_orderid", shunLongParams.payOrderid);
		model.addAttribute("pay_applydate", shunLongParams.payApplydate);
		model.addAttribute("pay_bankcode", shunLongParams.payBankcode);
		model.addAttribute("pay_notifyurl", shunLongParams.payNotifyurl);
		model.addAttribute("pay_callbackurl", shunLongParams.payCallbackurl);
		model.addAttribute("pay_amount", shunLongParams.payAmount);
		model.addAttribute("pay_productname", shunLongParams.payProductname);
		model.addAttribute("pay_md5sign", shunLongParams.payMd5sign);
		model.addAttribute("submitUrl", "http://nan53456.cn/Pay_Index.html");
		return "shunLong";
	}
	
	

	@GetMapping("alipayRedEnvelope/{id}")
	public String alipayRedEnvelope(@PathVariable String id, ModelMap model) {
		String paramJson = tokenClient.getPayParams(id);
		LOG.info("订单:" + id + "订单内容=" + paramJson);
		if (StringUtils.isBlank(paramJson)) {
			return "errorTransit";
		}
		String url = "alipays://platformapi/startapp?" + paramJson;
		StringBuffer urlA = new StringBuffer();
		StringSort stringSort = new StringSort();
		@SuppressWarnings("static-access")
		Map<String, Object> str = stringSort.getUrlParams(paramJson);
		urlA.append("alipays://platformapi/startapp?appId=20000167&forceRequest=0&returnAppId=recent&tLoginId=")
				.append(str.get("chatLoginId")).append("&tUnreadCount=0&tUserId=").append(str.get("chatUserId")).append("&tUserType=1");
		model.addAttribute("moneryA", "￥" + str.get("money"));
		model.addAttribute("remarkA", "充值单号：" + str.get("remark"));
		model.addAttribute("url", url);
		model.addAttribute("urlA", urlA.toString());
		model.addAttribute("code", "A"+id);
		return "alipayAI";
	}

	@GetMapping("alipayRedEnvelope/qr/{code}")
	public void alipayRedEnvelopeQArCode(@PathVariable String code, HttpServletResponse response) {
		String paramJson = tokenClient.getPayParams(code);
		try (OutputStream out = response.getOutputStream()) {
			createQrCode(paramJson, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	@RequestMapping("codeUrl")
	public String codeUrl(HttpServletRequest requestR, ModelMap model) {
		String id = requestR.getParameter("id");
		String monery = requestR.getParameter("monery");
		String remarkA = requestR.getParameter("remark");
		if (StringUtils.isBlank(id)) {
			return "errorTransit";
		}
		model.addAttribute("code", id);
		model.addAttribute("monery", "￥ " + monery);
		model.addAttribute("remarkA","充值单号："+ remarkA);
		return "urlCode";
	}
	
	@RequestMapping("codeUrl/qr/{code}")
	public void codeUrl(@PathVariable String code, HttpServletResponse response) {
		String paramJson = tokenClient.getPayParams("S-"+code);
		try (OutputStream out = response.getOutputStream()) {
			createQrCode(paramJson, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private static String getMonery(String msg) {
		String[] amounts = extractAmountMsg(msg);
		String ss = "";
		StringSort stringSort = new StringSort();
		Pattern pattern = Pattern.compile("(([1-9][0-9]*)\\.([0-9]{2}))|[0]\\.([0-9]{2})");
		for (int i = 0; i < amounts.length; i++) {

			if (StringUtils.isBlank(amounts[i])) {
				continue;
			}
			Matcher match = pattern.matcher(amounts[i]);
			if (!match.matches()) {
				continue;
			}
			if (i == 0 || StringUtils.isBlank(ss)) {
				ss = amounts[i];
				continue;
			}
			if (Long.valueOf(stringSort.yuan2FenInt(ss)) - Long.valueOf(stringSort.yuan2FenInt(amounts[i])) > 0) {
				ss = amounts[i];
			}
		}
		return ss;
	}

	public static String[] extractAmountMsg(String ptCasinoMsg) {
		String returnAmounts[] = new String[4];
		ptCasinoMsg = ptCasinoMsg.replace("，", " ");
		String[] amounts = ptCasinoMsg.split(" ");
		for (int i = 0; i < amounts.length; i++) {
			Pattern p = Pattern.compile("(\\d+\\.\\d+)");
			Matcher m = p.matcher(amounts[i]);
			if (m.find()) {
				returnAmounts[i] = m.group(1);
			} else {
				p = Pattern.compile("(\\d+)");
				m = p.matcher(amounts[i]);
				if (m.find()) {
					returnAmounts[i] = m.group(1);
				}
			}
		}
		return returnAmounts;
	}

}
