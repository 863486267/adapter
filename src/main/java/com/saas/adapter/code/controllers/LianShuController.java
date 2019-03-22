package com.saas.adapter.code.controllers;


import com.alibaba.fastjson.JSONObject;

import com.saas.adapter.entity.LianShuEntity;
import com.saas.adapter.entity.NotifyEntity;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.OrderReturnMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
@RestController
@RequestMapping(value = "/lianshu",produces = "application/json;charset=utf-8")
public class LianShuController {
    @Autowired
    RestTemplate template;
    @Autowired
    public OrderReturnMain orderReturnMain;

    @PostMapping("/pay")
    public OrderReturn lianshu(@RequestBody Parameter parameter) {

        String url = "http://b2c.uranuspay.com/agentB2c/quickPaymentAuth.action";
        String transCode = "001";
        String service = "0002";
        String reqDate = getDateymd(new Date());
        String transAmount = "";
        try {
            transAmount = changeF2Y(parameter.order.money);
        } catch (Exception e1) {
            e1.printStackTrace();
            return orderReturnMain.failReturn(e1.getLocalizedMessage());
        }
        String bgReturnUrl = "http://47.106.223.127:8201/lianshu/notifyurl";
        String openId = "o5x30vu9BiDf5enKxTJsxfUo2X6w";
        String reqTime = getDatehms(new Date());
        String customerNo = "290260000001";
        String externalId = parameter.order.no;
        String requestIp = "47.106.223.127";

        String signs = "bgReturnUrl=" + bgReturnUrl + "&customerNo=" + customerNo + "&externalId=" + externalId + "&openId=" + openId
                + "&reqDate=" + reqDate + "&reqTime=" + reqTime + "&requestIp=" + requestIp + "&service=" + service + "&transAmount=" +
                transAmount + "&transCode=" + transCode + "&key=783723B6D4DA4E5DBD5D1A9EFB39925B";
        String sign = md5Hash(signs).toUpperCase();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject request = new JSONObject();
        request.put("transCode", transCode);
        request.put("service", service);
        request.put("reqDate", reqDate);
        request.put("transAmount", transAmount);
        request.put("bgReturnUrl", bgReturnUrl);
        request.put("openId", openId);
        request.put("reqTime", reqTime);
        request.put("customerNo", customerNo);
        request.put("externalId", externalId);
        request.put("requestIp", requestIp);
        request.put("sign", sign);

        HttpEntity<String> requestEntity =
                new HttpEntity<>(request.toJSONString(), headers);
        ResponseEntity<String> response = null;
        try {
            response = template.exchange(url, HttpMethod.POST,
                    requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(String.valueOf(response));
        String string = String.valueOf(response);
        String e = string.substring(string.indexOf("externalId=") + 11, string.indexOf("&amount"));
        String a = string.substring(string.indexOf("amount=") + 7, string.indexOf("&customerNo"));
        String s = string.substring(string.indexOf("sign=") + 5, string.indexOf("\",\"externalId"));
        log.info(a);
        log.info("e:" + e);
        log.info("s:" + s);
        String urls = "https://weixin.sugenepay.com?externalId=" + e + "%26amount=" + a + "%26customerNo=290260000001%26name=充值%26requestId=" + parameter.order.no + "%26sign=" + s;
        //String urls = "http://47.106.223.127:8201/lianshu/pays?data=" + e + "a" + a + "r" + parameter.order.no + "s" + s;
        return orderReturnMain.successReturn(urls);

    }

//    @GetMapping("/pays")
//    public void pays(String data, HttpServletResponse response) throws IOException {
//        String e = data.substring(0, data.indexOf("a"));
//        String a = data.substring(data.indexOf("a") + 1, data.indexOf("r"));
//        String r = data.substring(data.indexOf("r") + 1, data.indexOf("s"));
//        String s = data.substring(data.indexOf("s") + 1);
//        log.info(a);
//        String url = "https://weixin.sugenepay.com?externalId=" + e + "&amount=" + a + "&customerNo=290260000001&name=充值&requestId=" + r + "&sign=" + s;
//        log.info("url:" + url);
//        response.sendRedirect(url);
//
//    }

    @PostMapping("/notifyurl")
    public String notifyurl(NotifyEntity entity) {
        String code = entity.getCode();
        String message = entity.getMessage();
        String externalId = entity.getExternalId();
        String seqno = entity.getSeqno();
        String amount = entity.getAmount();
        String customerNo = entity.getCustomerNo();
        String sign = entity.getSign();
        String string = "amount=" + amount + "&code=" + code + "&customerNo=" + customerNo + "&externalId=" + externalId + "&message=" + message
                + "&seqno=" + seqno + "&key=783723B6D4DA4E5DBD5D1A9EFB39925B";
        String signs = md5Hash(string).toUpperCase();
        log.info("sign:" + sign);
        log.info("signs:" + signs);
        if (sign.equals(signs)) {
            if (code.equals("00")) {
                return "SUCCESS";
            }
            return "failure";
        }
        return "failure";
    }

    @GetMapping("/returnurl")
    public void returnurl(LianShuEntity entity) {

        String url = "http://qry.uranuspay.com/onlinepayQry/gateway.action";
//        String transCode = entity.getTransCode();
//        String externalId = entity.getExternalId();
//        String customerNo = entity.getCustomerNo();
//        String reqDate = entity.getReqDate();
//        String requestIp = entity.getRequestIp();


        String transCode = "002";
        String externalId = "121212232323";
        String customerNo = "290260000001";
        String reqDate = "20170721";
        String requestIp = "47.106.223.127";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject json = new JSONObject();
        json.put("transCode", transCode);
        json.put("externalId", externalId);
        json.put("customerNo", customerNo);
        json.put("reqDate", reqDate);
        json.put("requestIp", requestIp);
        String signs = "customerNo=" + customerNo + "&externalId=" + externalId + "&reqDate=" + reqDate + "&requestIp=" + requestIp
                + "&transCode=" + transCode + "&key=783723B6D4DA4E5DBD5D1A9EFB39925B";
        String sign = md5Hash(signs).toUpperCase();
        json.put("sign", sign);
        HttpEntity<String> requestEntity =
                new HttpEntity<>(json.toJSONString(), headers);
        ResponseEntity<String> response = null;
        try {
            response = template.exchange(url, HttpMethod.POST,
                    requestEntity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(String.valueOf(response));


    }

    /**
     * 返回时间字符串 yyyy年MM月dd日 类型
     *
     * @param dateTime
     * @return
     */
    private String getDateymd(Date dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(dateTime).toString();
    }

    /**
     * 返回时间字符串 yyyy年MM月dd日 类型
     *
     * @param dateTime
     * @return
     */
    private String getDatehms(Date dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        return sdf.format(dateTime).toString();
    }

    public String md5Hash(String string) {
        try {
            StringBuilder sb = new StringBuilder();
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] digest = md5.digest(string.getBytes("utf8"));
            for (byte b : digest) {
                //把每个字节转换成16进制数
                int d = b & 0xff;//0x00 00 00 00 ff
                String hexString = Integer.toHexString(d);
                if (hexString.length() == 1) {
                    hexString = "0" + hexString;
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
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
