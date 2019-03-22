package com.saas.adapter.code.controllers;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
@RequestMapping("/baotian")
@Slf4j
public class BaoTianPay {
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
    public void pay(@RequestBody Parameter parameter) throws Exception {

        String url="http://yjpayapi.yoopard.cn/api/Italy/order/unionPay";

        String p_Amount=changeF2Y(parameter.order.money);
//        String p_Amount="2000";
        String p_ChildChannelNo="Milan";
        String p_MainChannelNo="Italy";
        String p_MerchantNo="20190319130009171374";
        String p_NotifyUrl="http://47.106.184.173:8984/baotian/notify";
//        String p_OrderNo=getRandomString(15);
        String p_OrderNo=parameter.order.id;
        String p_ProductName="付款";
        String p_ReturnUrl="http://47.106.184.173:8984/baotian/notify";
        String signs="p_Amount="+p_Amount+"&p_ChildChannelNo="+p_ChildChannelNo+"&p_MainChannelNo="+p_MainChannelNo+"&p_MerchantNo="+p_MerchantNo+"&p_NotifyUrl="+p_NotifyUrl+"&p_OrderNo="+p_OrderNo+"&p_ProductName="+p_ProductName+"&p_ReturnUrl="+p_ReturnUrl+"&AppId=ax2WpOg6e7qGoUCs1Oy9QITq8dT5dasJwrdrkAnE&AppKey=OHQheZD6DnPeUBv62VpfLPcOhCadXIg3Wqp5jMbP";
        log.info(signs);
        String sign=md5(signs);
        log.info(sign);
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("p_Amount",p_Amount);
        jsonObject.put("p_ChildChannelNo",p_ChildChannelNo);
        jsonObject.put("p_MainChannelNo",p_MainChannelNo);
        jsonObject.put("p_MerchantNo",p_MerchantNo);
        jsonObject.put("p_NotifyUrl",p_NotifyUrl);
        jsonObject.put("p_OrderNo",p_OrderNo);
        jsonObject.put("p_ProductName",p_ProductName);
        jsonObject.put("p_ReturnUrl",p_ReturnUrl);
        jsonObject.put("sign",sign);
        HttpEntity<String> httpEntity=new HttpEntity<>(jsonObject.toJSONString(),headers);
        ResponseEntity responseEntity=restTemplate.exchange(url,HttpMethod.POST,httpEntity,String.class);
        log.info(String.valueOf(responseEntity));
        Gson gson = new Gson();
        log.info(String.valueOf(responseEntity));

        String result=String.valueOf(responseEntity);
        result=result.substring(result.indexOf("{"),result.lastIndexOf(",{Server"));
        log.info(result);
        Map<?, ?> map = gson.fromJson(result, Map.class);
        Map<?, ?> response = (Map<?, ?>) map.get("response");
        String returnUrl=String.valueOf(response.get("p_PayHtml"));
        log.info(returnUrl);
       // tokenClient.savePayUrl(p_OrderNo, parameter.order.orderConfig.notifyUrl, 50);
        //return orderReturnMain.successReturn(returnUrl, "充值金额", parameter.order.money);
    }

    @PostMapping("/notify")
    public String dd(@RequestBody String json){
        Gson gson = new Gson();
        Map<?, ?> map = gson.fromJson(json, Map.class);
        Map<?, ?> response = (Map<?, ?>) map.get("response");
        String signs="p_Amount="+response.get("p_Amount").toString()+"&p_ChildChannelNo="+response.get("p_ChildChannelNo").toString()+"&p_MainChannelNo="+response.get("p_MainChannelNo").toString()+"&p_MerchantNo="+response.get("p_MerchantNo").toString()+"&p_OrderNo="+response.get("p_OrderNo").toString()+"&p_PayHtml="+response.get("p_PayHtml").toString()+"&AppId=ax2WpOg6e7qGoUCs1Oy9QITq8dT5dasJwrdrkAnE&AppKey=OHQheZD6DnPeUBv62VpfLPcOhCadXIg3Wqp5jMbP";
        String sign=md5(signs).toUpperCase();
        String notifyUrl = tokenClient.getPayParams(response.get("p_OrderNo").toString());
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, json, String.class);
        }
        log.info("三方："+sign);
        log.info("三方："+response.get("sign").toString());
        if(sign.equals(response.get("sign").toString())) {
            if (map.get("success").toString().equals("true")) {
                return "true";
            }
            return "false";
        }
        return "error";

    }


    @PostMapping("/notifys")
    public CallbackResult suinotifyurl(@RequestBody String str) throws Exception {
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
        money=changeF2Y(money);
        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("p_Amount"));
        String o= String.valueOf(mapbody.get("p_OrderNo"));
        m=m.substring(0,m.indexOf("."));
        log.info("new钱:"+m);
        log.info("old钱:"+money);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }
    public static String gettime(){
        Date date=new Date();
        long times=date.getTime();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(times);
//        time=time.replaceAll("-","");
//        time=time.replaceAll(" ","");
//        time=time.replaceAll(":","");
        return time;
    }

    public static String md5(String s){
        MessageDigest messageDigest= null;
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            String md = new BigInteger(1, messageDigest.digest(s.getBytes("utf-8"))).toString(16);
            return md;
        } catch (UnsupportedEncodingException e) {
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
