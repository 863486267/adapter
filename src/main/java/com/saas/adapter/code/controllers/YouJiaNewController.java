package com.saas.adapter.code.controllers;


import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.entity.JiaLianEntity;
import com.saas.adapter.entity.YouJiaNewEntity;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/youjianew")
@Slf4j
public class YouJiaNewController {


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

        String mchId = "20000075";
        String appId = "655179f489494bd2a4360bc508281d1b";
        String productId = "8025";
        String mchOrderNo = parameter.order.no;
        String currency = "cny";
        String amount = parameter.order.money;
        String returnUrl = "http://47.106.223.127:8983/youjianew/notify";
        String notifyUrl = "http://47.106.223.127:8983/youjianew/notify";
        String subject = "付款";
        String body = "fukuan";


        String s = "amount=" + amount + "&appId=" + appId + "&body=" + body + "&currency=" + currency + "&mchId=" + mchId + "&mchOrderNo=" + mchOrderNo + "&notifyUrl=" + notifyUrl + "&productId=" + productId + "&returnUrl=" + returnUrl + "&subject=" + subject + "&key=UK3VCVFQKGORRIA5FEAEZMOKBFEOA9XGEQ7SY1GICZWQCJEJEQ3UOO7CPZQ2RED52JLR3DUS71OERSXWEFWWG5QCXYJVTOGG8FG5GS6DUO9IZPXCZ1KPK0QRKXBC6KIZ";

        log.info(s);
        String sign = md5(s).toUpperCase();
        log.info(sign);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("mchId", mchId);
        map.add("appId", appId);
        map.add("productId", productId);
        map.add("mchOrderNo", mchOrderNo);
        map.add("currency", currency);
        map.add("amount", amount);
        map.add("returnUrl", returnUrl);
        map.add("notifyUrl", notifyUrl);
        map.add("subject", subject);
        map.add("body", body);
        map.add("sign", sign);


        HttpEntity<MultiValueMap> entity = new HttpEntity<>(map, headers);
        ResponseEntity response = template.exchange("https://pay.weechang.xyz/api/pay/create_order", HttpMethod.POST, entity, String.class);

        log.info(String.valueOf(response));
        String url = String.valueOf(response);
        log.info(url);
        if (!(url.contains("https"))){
            return null;
        }
        url=url.substring(url.lastIndexOf("https"),url.lastIndexOf("}")-2);
        url=url.substring(0,url.indexOf("\""));
        log.info("地址："+url);
        tokenClient.savePayUrl(parameter.order.no, parameter.order.orderConfig.notifyUrl, 50);

        return orderReturnMain.successReturn(url, "充值金额", parameter.order.money);
    }

    @PostMapping("/notify")
    public String dd(YouJiaNewEntity entity){
         String income=entity.getIncome();
         String payOrderId=entity.getPayOrderId();
         String mchId=entity.getMchId();
         String appId=entity.getAppId();
         String productId=entity.getProductId();
         String mchOrderNo=entity.getMchOrderNo();
         String amount=entity.getAmount();
         String status=entity.getStatus();
         String channelOrderNo=entity.getChannelOrderNo();
         String channelAttach=entity.getChannelAttach();
         String param1=entity.getParam1();
         String param2=entity.getParam2();
         String paySuccTime=entity.getPaySuccTime();
         String sign=entity.getSign();


        String notifyUrl = tokenClient.getPayParams(mchOrderNo);
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, entity, String.class);
        }
        log.info("三方："+sign);
        log.info("三方："+entity.getSign());

        log.info(entity.getStatus());
            if (entity.getStatus().equals("2")) {
                log.info("success");
                return "success";
            }
            log.info("failure");
            return "failure";

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

        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("amount"));
        String o= String.valueOf(mapbody.get("channelOrderNo"));
        log.info("new钱:"+m);
        log.info("old钱:"+money);
        money=money.substring(0,money.indexOf("."));
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }



    public static String md5(String s) {
        MessageDigest messageDigest = null;
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
