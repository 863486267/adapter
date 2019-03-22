package com.saas.adapter.code.controllers;


import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.entity.PddEntity;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.Analysis;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/youle")
@Slf4j
public class PddController {
    @Autowired
    RestTemplate template;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private TokenClient tokenClient;
    @Autowired
    public OrderReturnMain orderReturnMain;
    @Autowired
    public SaasNotifyParams saasNotifyParams;

    @PostMapping("/pay")
    public OrderReturn pay(@RequestBody Parameter parameter) throws Exception {

        String keyValue = "ei864yqi10gebsunq639tup6l4n53vq6";
        String url = "http://www.w5pc.cn/Pay_Index.html";
        String pay_bankcode = "925";   //'银行编码
        String pay_memberid = "190406400";//商户id
        String pay_orderid = toOrderId();//20位订单号 时间戳+6位随机字符串组成
        String pay_applydate = toTime();//yyyy-MM-dd HH:mm:ss
        String pay_notifyurl = "http://47.106.223.127:8983/youle/notify";//通知地址
        String pay_callbackurl = "http://47.106.223.127:8983/youle/notify";//回调地址
        log.info(pay_callbackurl);
        log.info(parameter.order.money);
        String pay_amount = changeF2Y(parameter.order.money);
        log.info(pay_amount);
        String pay_md5sign = null;
        String pay_productname = "充值";
        String stringSignTemp = "pay_amount=" + pay_amount + "&pay_applydate=" + pay_applydate + "&pay_bankcode=" + pay_bankcode + "&pay_callbackurl=" + pay_callbackurl + "&pay_memberid=" + pay_memberid + "&pay_notifyurl=" + pay_notifyurl + "&pay_orderid=" + pay_orderid + "&key=" + keyValue + "";
        try {
            pay_md5sign = md5(stringSignTemp).toUpperCase();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info(pay_md5sign);
        log.info("订单号1：" + pay_orderid);
        log.info(pay_applydate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("pay_bankcode", pay_bankcode);
        map.add("pay_memberid", pay_memberid);
        map.add("pay_orderid", pay_orderid);
        map.add("pay_applydate", pay_applydate);
        map.add("pay_notifyurl", pay_notifyurl);
        map.add("pay_callbackurl", pay_callbackurl);
        map.add("pay_amount", pay_amount);
        map.add("pay_productname", pay_productname);
        map.add("pay_md5sign", pay_md5sign);
        log.info("进入支付");
        HttpEntity<MultiValueMap> entity = new HttpEntity<>(map, headers);
        ResponseEntity responseEntity = template.exchange(url, HttpMethod.POST, entity, String.class);
        String response = String.valueOf(responseEntity);
        log.info(response);
        if (!response.contains(".jpg")) {
            log.info("下单失败");
            return null;
        }
        String qr = response.substring(response.indexOf("http:"), response.indexOf(".jpg") + 4);
        log.info("qr:" + qr);
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
//        String urls="http://47.106.220.74:8186/wechat/pay?str="+qr+pay_amount;
//        log.info(urls);
        return orderReturnMain.successReturn(Analysis.getAnalysis(qr), "充值金额", parameter.order.money);
    }

    @PostMapping("/notify")
    public String pddpingduoduo(PddEntity pddEntity) throws UnsupportedEncodingException {
        String memberid = pddEntity.getMemberid();
        String orderid = pddEntity.getOrderid();
        String amount = pddEntity.getAmount();
        String transaction_id = pddEntity.getTransaction_id();
        String datetime = pddEntity.getDatetime();
        String returncode = pddEntity.getReturncode();
        String signs = "amount=" + amount + "&datetime=" + datetime + "&memberid=" + memberid + "&orderid=" + orderid + "&returncode=" + returncode + "&transaction_id=" + transaction_id + "&key=ei864yqi10gebsunq639tup6l4n53vq6";
        log.info(signs);
        log.info(pddEntity.getSign());
        String sign = md5(signs).toUpperCase();
        log.info("订单号2:" + orderid);
        String notifyUrl = tokenClient.getPayParams(pddEntity.getOrderid());
        if (sign.equals(pddEntity.getSign())) {
            restTemplate.postForObject(notifyUrl, pddEntity, String.class);
            if (pddEntity.getReturncode().equals("00")) {
                return "OK";
            }
            return "failure";
        }
        return "error";
    }

    @PostMapping("/notifys")
    public CallbackResult pddnotifyurl(@RequestBody String string) throws Exception {
        log.info(string);
        Gson gson = new Gson();
        Map<?, ?> map = gson.fromJson(string, Map.class);
        Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
        String moneyold = String.valueOf(orderMap.get("money"));


        moneyold = moneyold.substring(0, moneyold.indexOf("."));
        log.info(moneyold);
        moneyold = changeF2Y(moneyold);
        log.info(moneyold);
        if (moneyold.contains(".")) {
            moneyold = moneyold.substring(0, moneyold.indexOf("."));
        }
        String money = string.substring(string.indexOf("amount") + 11, string.indexOf("transaction_id") - 5);
        money = money.substring(0, money.indexOf("."));
        String order = string.substring(string.indexOf("orderid") + 12, string.indexOf("amount") - 5);
        log.info(moneyold);
        log.info(money);
        log.info(order);
        if (moneyold.equals(money)) {
            return saasNotifyParams.successParams(order, "success");
        }
        return saasNotifyParams.errorParams("订单金额不正确", "ok");
    }


    public static String toOrderId() {
        Date date = new Date();
        String time = String.valueOf(date.getTime());
        int i = (int) (Math.random() * 9999999);
        return time + i;
    }

    public static String toTime() {
        Date date = new Date();
        long s = date.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(s);
        return time;
    }

    public static String md5(String s) throws UnsupportedEncodingException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            String md = new BigInteger(1, messageDigest.digest(s.getBytes("utf-8"))).toString(16);
            return md;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
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
