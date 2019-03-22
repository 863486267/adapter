package com.saas.adapter.code.controllers;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.entity.SuiYinLianEntity;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/suiyinlian")
public class SuiYinLianController {

    @Autowired
    RestTemplate template;
    @Autowired
    OrderReturnMain orderReturnMain;
    @Autowired
    private TokenClient tokenClient;
    @Autowired
    public SaasNotifyParams saasNotifyParams;

    // sweepCode
    @PostMapping("/pay")
    public OrderReturn ylpay(@RequestBody Parameter parameter) {

        String url = "https://paygw.softbankc.com/gateway";
        String key1 = "960a217eb5e94b10af42190aa95c1bba";
        String notify_url = "http://47.106.223.127:8983/suiyinlian/notify";
        String agency_id = "9000049640";
        log.info("请求进入");
        String m = parameter.order.money;
        log.info("钱"+m);
        int fee = Integer.parseInt(m);
        String service = "up.gateway.qrpay";
        String tradeno = parameter.order.no;
        log.info("订单号1"+tradeno);
        String version = "2.0";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> map = new TreeMap<>();
        map.put("agency_id", agency_id);
        map.put("fee", fee);
        map.put("notify_url", notify_url);
        map.put("trade_no", tradeno);
        map.put("service", service);
        map.put("version", version);
        map.put("nonce_str", UUID.randomUUID().toString().replaceAll("-", ""));

        StringBuilder tmp = new StringBuilder(1024);
        for (String key : map.keySet()) {
            if (map.get(key) != null && !"".equals(map.get(key))) {
                tmp.append(key).append("=").append(map.get(key).toString());
                tmp.append("&");
            }
        }
        tmp.append("key=").append(key1);
        map.put("sign", SHA1Encode(tmp.toString(), "UTF-8").toUpperCase());

        try {
            String wxreturn = null;
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/json; charset=UTF-8");
            RequestConfig requestConfig = RequestConfig.custom().build();
            httppost.setConfig(requestConfig);
            httppost.setEntity(new StringEntity(JSONObject.toJSONString(map), "UTF-8"));
            CloseableHttpResponse response = httpclient.execute(httppost);
            org.apache.http.HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                String temp;
                StringBuilder res = new StringBuilder(1024);
                while ((temp = br.readLine()) != null) {
                    res.append(temp);
                }
                wxreturn = res.toString();
                br.close();
            }
            EntityUtils.consume(entity);
            response.close();
            httpclient.close();
            if (!wxreturn.contains("https")) {
                return null;
            }
            String urls = wxreturn.substring(wxreturn.indexOf("https"), wxreturn.indexOf("status") - 3);
            log.info(urls);
            log.info(wxreturn);
            log.info("saas回调地址："+parameter.order.orderConfig.notifyUrl);
            tokenClient.savePayUrl(parameter.order.no, parameter.order.orderConfig.notifyUrl, 50);
            return orderReturnMain.successReturn(urls,"银联支付",parameter.order.money);
        } catch (Exception e) {
        }
        return null;
    }


    @PostMapping("/notify")
    public String suinotify(@RequestBody SuiYinLianEntity entity) {

        String agency_id = entity.getAgency_id();
        String payed_time=entity.getPayed_time();
        String service = entity.getService();
        String nonce_str = entity.getNonce_str();
        String trade_no = entity.getTrade_no();
        String trade_status = entity.getTrade_status();
        String transaction_id = entity.getTransaction_id();
        String trade_barcode = entity.getTrade_barcode();
        int fee = entity.getFee();
        int cash = entity.getCash();
        int coupon = entity.getCoupon();
        log.info(agency_id);
        log.info(payed_time);
        log.info(service);
        log.info(nonce_str);
        log.info(trade_no);
        log.info(trade_status);
        log.info(transaction_id);
        log.info(trade_barcode);
        log.info(String.valueOf(fee));
        log.info(String.valueOf(cash));
        log.info(String.valueOf(coupon));

        String signs = "agency_id=" + agency_id + "&cash=" + cash + "&coupon" + coupon + "&fee" + fee + "&nonce_str" + nonce_str
                + "&payed_time="+payed_time+"&service" + service + "&trade_barcode" + trade_barcode + "&trade_no" + trade_no
                + "&trade_status" + trade_status + "&transaction_id" + transaction_id + "&key=960a217eb5e94b10af42190aa95c1bba";
        String sign = SHA1Encode(signs, "UTF-8").toUpperCase();
        String old = entity.getSign();
        log.info(sign);
        log.info(old);
        String notifyUrl = tokenClient.getPayParams(trade_no);
        log.info("订单号2"+trade_no);
        log.info("回调地址2"+notifyUrl);
       // if (sign.equals(old)) {
            template.postForObject(notifyUrl, entity, String.class);
            if (entity.getTrade_status().equals("SUCCESS")) {
                return "SUCCESS";
            }
            return "failure";
       // }
      //  return "error";
    }

    @PostMapping("/notifys")
    public CallbackResult suinotifyurl(@RequestBody String str) {
        log.info(str);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        Gson gson = new Gson();
        Map<?, ?> map = gson.fromJson(str, Map.class);
        Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
        String money = String.valueOf(orderMap.get("money"));
        if(money.contains(".")){
            money=money.substring(0,money.indexOf("."));
        }

        String m=str.substring(str.indexOf("fee")+6,str.indexOf("cash")-3);
        String o=str.substring(str.indexOf("trade_no")+13,str.indexOf("trade_status")-5);
        log.info(m);
        log.info(money);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }


    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String SHA1Encode(String origin, String enc) {
        String resultString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            resultString = byteArrayToHexString(md.digest(origin.getBytes(enc)));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
        }
        return resultString;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder(64);
        for (byte aB : b) {
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }


}
