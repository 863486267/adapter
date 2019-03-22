package com.saas.adapter.code.controllers;


import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.entity.DingDingEntity;
import com.saas.adapter.entity.JiaLianEntity;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.Analysis;
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
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/jialian")
@Slf4j
public class JiaLianController {

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
        String pay_memberid="10173";
        String pay_orderid=parameter.order.no;
        String pay_notifyurl="http://47.106.223.127:8983/jialian/notify";
        String pay_amount=changeF2Y(parameter.order.money);
        String pay_applydate=gettime();
        String pay_bankcode="982";
        String pay_callbackurl="http://47.106.223.127:8983/jialian/notify";
        String signs="pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode +"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl+"&pay_orderid="+pay_orderid+"&key=m86iezivtgy5od6bou1q4yql6z4li6i2";
        log.info(signs);
        //pay_amount=268&pay_applydate=2018-08-14 21:02:09&pay_bankcode=950&pay_callbackurl=//yourdomain/callback.php&pay_memberid=10030&pay_notifyurl=http://oem.9580buy.com/notify/MallNotice/tst?appid=10000&pay_orderid=20180814210209922985”
       //pay_amount=100&pay_applydate=2019-03-14 14:42:21&pay_bankcode=982&pay_callbackurl=http://47.106.223.127:8994/jialian/notify&pay_memberid=10173&pay_notifyurl=http://47.106.223.127:8994/jialian/notify&pay_orderid=124545649858574&key=m86iezivtgy5od6bou1q4yql6z4li6i2
        String pay_md5sign=md5(signs).toUpperCase();
        log.info(":"+pay_applydate);
        log.info(":"+pay_md5sign);


        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String ,String> map=new LinkedMultiValueMap<>();
        map.add("pay_memberid",pay_memberid);
        map.add("pay_orderid",pay_orderid);
        map.add("pay_notifyurl",pay_notifyurl);
        map.add("pay_amount",pay_amount);
        map.add("pay_applydate",pay_applydate);
        map.add("pay_bankcode",pay_bankcode);
        map.add("pay_callbackurl",pay_callbackurl);
        map.add("pay_md5sign",pay_md5sign);

        HttpEntity<MultiValueMap> entity=new HttpEntity<>(map,headers);
        ResponseEntity responseEntity=template.exchange("http://pay.hqyh2014.cn/Pay_Index", HttpMethod.POST,entity,String.class);
        log.info(String.valueOf(responseEntity));
        String url=String.valueOf(responseEntity);
        url=url.substring(url.indexOf("http:"),url.indexOf("\"}"));
        url=url.replaceAll("\\\\","");
        log.info(url);
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);

        return orderReturnMain.successReturn(url, "充值金额", parameter.order.money);
    }

    @PostMapping("/notify")
    public String dd(JiaLianEntity entity){
        String orderid=entity.getOrderid();
        String amount=entity.getAmount();
        String memberid=entity.getMemberid();
        String datetime=entity.getDatetime();
        String returncode=entity.getReturncode();
        String attach=entity.getAttach();
        String transaction_id=entity.getTransaction_id();

        log.info(orderid);
        log.info(amount);
        String signs="amount="+amount+"&datetime="+datetime+"&memberid="+memberid+"&orderid="+orderid
                +"&returncode="+returncode+"&transaction_id="+transaction_id+"&key=m86iezivtgy5od6bou1q4yql6z4li6i2";

        String sign=md5(signs).toUpperCase();
        String notifyUrl = tokenClient.getPayParams(orderid);
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, entity, String.class);
        }
        log.info("三方："+sign);
        log.info("三方："+entity.getSign());
        if(sign.equals(entity.getSign())) {
            if (entity.getReturncode().equals("00")) {
                return "ok";
            }
            return "failure";
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
        String m= String.valueOf(mapbody.get("amount"));
        String o= String.valueOf(mapbody.get("transaction_id"));
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
        DateFormat format=new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
