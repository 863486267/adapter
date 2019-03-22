package com.saas.adapter.code.controllers;


import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.entity.NingMnegEntity;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.MD5;
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

import java.util.HashMap;
import java.util.Map;

import static com.saas.adapter.tools.MD5.changeF2Y;


@RestController
@RequestMapping("/ningmeng")
@Slf4j
public class NingMengController {


    @Autowired
    RestTemplate template;

    @Autowired
    private TokenClient tokenClient;

    @Autowired
    public OrderReturnMain orderReturnMain;
    @Autowired
    public SaasNotifyParams saasNotifyParams;

    Map htmlTextMap = new HashMap();
    //支付宝扫码
    @PostMapping("/wechatH5pay")
    public OrderReturn wechatH5pay(@RequestBody Parameter parameter) throws Exception {
        String pay_memberid="10092";
        String pay_orderid=parameter.order.no;
        String pay_applydate=MD5.getR(0);
        String pay_bankcode="901";
        String pay_notifyurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_callbackurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_amount=changeF2Y(parameter.order.money);
        String signs="pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode
                +"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl
                +"&pay_orderid="+pay_orderid+"&key=9adgikusuma84d3inaaee7aac638q6w1";
        String pay_md5sign= MD5.md5(signs).toUpperCase();
        String pay_productname="test";
        System.out.println(pay_orderid);
        System.out.println(pay_applydate);
        System.out.println(pay_md5sign);



        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String ,String> map=new LinkedMultiValueMap<>();
        map.add("pay_memberid",pay_memberid);
        map.add("pay_orderid",pay_orderid);
        map.add("pay_applydate",pay_applydate);
        map.add("pay_bankcode",pay_bankcode);
        map.add("pay_notifyurl",pay_notifyurl);
        map.add("pay_callbackurl",pay_callbackurl);
        map.add("pay_amount",pay_amount);
        map.add("pay_md5sign",pay_md5sign);
        map.add("pay_productname",pay_productname);
        HttpEntity<MultiValueMap> entity=new HttpEntity<>(map,headers);
        ResponseEntity responseEntity=template.exchange("http://all.nn-ex.com/Pay_Index.html", HttpMethod.POST,entity,String.class);
        log.info(String.valueOf(responseEntity));
        String htmlText=String.valueOf(responseEntity);
        htmlText=htmlText.substring(htmlText.indexOf(",")+1,htmlText.indexOf(",{Server=[nginx]"));
        htmlTextMap.put(pay_orderid,htmlText);
        log.info(htmlText);
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8984/ningmeng/obtain?htmlText="+htmlText;
        return orderReturnMain.successReturn("http://47.106.223.127:8983/ningmeng/payment?orderid="+pay_orderid, "充值金额", parameter.order.money);
    }

    @PostMapping("/wechatpay")
    public OrderReturn wechatpay(@RequestBody Parameter parameter) throws Exception {
        String pay_memberid="10092";
        String pay_orderid=parameter.order.no;
        String pay_applydate=MD5.getR(0);
        String pay_bankcode="902";
        String pay_notifyurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_callbackurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_amount=changeF2Y(parameter.order.money);
        String signs="pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode
                +"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl
                +"&pay_orderid="+pay_orderid+"&key=9adgikusuma84d3inaaee7aac638q6w1";
        String pay_md5sign= MD5.md5(signs).toUpperCase();
        String pay_productname="test";
        System.out.println(pay_orderid);
        System.out.println(pay_applydate);
        System.out.println(pay_md5sign);



        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String ,String> map=new LinkedMultiValueMap<>();
        map.add("pay_memberid",pay_memberid);
        map.add("pay_orderid",pay_orderid);
        map.add("pay_applydate",pay_applydate);
        map.add("pay_bankcode",pay_bankcode);
        map.add("pay_notifyurl",pay_notifyurl);
        map.add("pay_callbackurl",pay_callbackurl);
        map.add("pay_amount",pay_amount);
        map.add("pay_md5sign",pay_md5sign);
        map.add("pay_productname",pay_productname);
        HttpEntity<MultiValueMap> entity=new HttpEntity<>(map,headers);
        ResponseEntity responseEntity=template.exchange("http://all.nn-ex.com/Pay_Index.html", HttpMethod.POST,entity,String.class);
        log.info(String.valueOf(responseEntity));
        String htmlText=String.valueOf(responseEntity);
        htmlText=htmlText.substring(htmlText.indexOf(",")+1,htmlText.indexOf(",{Server=[nginx]"));
        htmlTextMap.put(pay_orderid,htmlText);
        log.info(htmlText);
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8984/ningmeng/obtain?htmlText="+htmlText;
        return orderReturnMain.successReturn("http://47.106.223.127:8983/ningmeng/payment?orderid="+pay_orderid, "充值金额", parameter.order.money);
    }

    @PostMapping("/alipaypay")
    public OrderReturn alipaypay(@RequestBody Parameter parameter) throws Exception {
        String pay_memberid="10092";
        String pay_orderid=parameter.order.no;
        String pay_applydate=MD5.getR(0);
        String pay_bankcode="903";
        String pay_notifyurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_callbackurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_amount=changeF2Y(parameter.order.money);
        String signs="pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode
                +"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl
                +"&pay_orderid="+pay_orderid+"&key=9adgikusuma84d3inaaee7aac638q6w1";
        String pay_md5sign= MD5.md5(signs).toUpperCase();
        String pay_productname="test";
        System.out.println(pay_orderid);
        System.out.println(pay_applydate);
        System.out.println(pay_md5sign);



        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String ,String> map=new LinkedMultiValueMap<>();
        map.add("pay_memberid",pay_memberid);
        map.add("pay_orderid",pay_orderid);
        map.add("pay_applydate",pay_applydate);
        map.add("pay_bankcode",pay_bankcode);
        map.add("pay_notifyurl",pay_notifyurl);
        map.add("pay_callbackurl",pay_callbackurl);
        map.add("pay_amount",pay_amount);
        map.add("pay_md5sign",pay_md5sign);
        map.add("pay_productname",pay_productname);
        HttpEntity<MultiValueMap> entity=new HttpEntity<>(map,headers);
        ResponseEntity responseEntity=template.exchange("http://all.nn-ex.com/Pay_Index.html", HttpMethod.POST,entity,String.class);
        log.info(String.valueOf(responseEntity));
        String htmlText=String.valueOf(responseEntity);
        htmlText=htmlText.substring(htmlText.indexOf(",")+1,htmlText.indexOf(",{Server=[nginx]"));
        htmlTextMap.put(pay_orderid,htmlText);
        log.info(htmlText);
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8984/ningmeng/obtain?htmlText="+htmlText;
        return orderReturnMain.successReturn("http://47.106.223.127:8983/ningmeng/payment?orderid="+pay_orderid, "充值金额", parameter.order.money);
    }

    @PostMapping("/alipayH5pay")
    public OrderReturn alipayH5pay(@RequestBody Parameter parameter) throws Exception {
        String pay_memberid="10092";
        String pay_orderid=parameter.order.no;
        String pay_applydate=MD5.getR(0);
        String pay_bankcode="904";
        String pay_notifyurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_callbackurl="http://47.106.223.127:8983/ningmeng/notify";
        String pay_amount=changeF2Y(parameter.order.money);
        String signs="pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode
                +"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl
                +"&pay_orderid="+pay_orderid+"&key=9adgikusuma84d3inaaee7aac638q6w1";
        String pay_md5sign= MD5.md5(signs).toUpperCase();
        String pay_productname="test";
        System.out.println(pay_orderid);
        System.out.println(pay_applydate);
        System.out.println(pay_md5sign);



        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String ,String> map=new LinkedMultiValueMap<>();
        map.add("pay_memberid",pay_memberid);
        map.add("pay_orderid",pay_orderid);
        map.add("pay_applydate",pay_applydate);
        map.add("pay_bankcode",pay_bankcode);
        map.add("pay_notifyurl",pay_notifyurl);
        map.add("pay_callbackurl",pay_callbackurl);
        map.add("pay_amount",pay_amount);
        map.add("pay_md5sign",pay_md5sign);
        map.add("pay_productname",pay_productname);
        HttpEntity<MultiValueMap> entity=new HttpEntity<>(map,headers);
        ResponseEntity responseEntity=template.exchange("http://all.nn-ex.com/Pay_Index.html", HttpMethod.POST,entity,String.class);
        log.info(String.valueOf(responseEntity));
        String htmlText=String.valueOf(responseEntity);
        htmlText=htmlText.substring(htmlText.indexOf(",")+1,htmlText.indexOf(",{Server=[nginx]"));
        htmlTextMap.put(pay_orderid,htmlText);
        log.info(htmlText);
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8984/ningmeng/obtain?htmlText="+htmlText;
        return orderReturnMain.successReturn("http://47.106.223.127:8983/ningmeng/payment?orderid="+pay_orderid, "充值金额", parameter.order.money);
    }




    @GetMapping("/payment")
    public String payment(String orderid){
        String htmlText=htmlTextMap.get(orderid).toString();
        htmlTextMap.remove(orderid);
        return htmlText;
    }


    @PostMapping("/notify")
    public String dd(NingMnegEntity entity){

        log.info("柠檬回调参数："+entity);

        log.info(entity.getOrderid());
        log.info(entity.getAmount());
        //"pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode
       // +"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"
        // &pay_notifyurl="+pay_notifyurl
               // +"&pay_orderid="+pay_orderid+"&key=9adgikusuma84d3inaaee7aac638q6w1";
        String signs="amount="+entity.getAmount()+"&datetime="+entity.getDatetime()+"&memberid="+entity.getMemberid()+"&orderid="+entity.getOrderid()+"&returncode="+entity.getReturncode()+"&transaction_id="+entity.getTransaction_id()+"&key=9adgikusuma84d3inaaee7aac638q6w1";

        log.info(signs);
        String sign=MD5.md5(signs).toUpperCase();
        String notifyUrl = tokenClient.getPayParams(entity.getOrderid());
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, entity, String.class);
        }
        log.info("我就加签："+sign);
        log.info("三方加签："+entity.getSign());
        if(sign.equals(entity.getSign())) {
            if (entity.getReturncode().equals("00")) {
                return "OK";
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
        double mo=Double.parseDouble(m)*100;
        double money1=Double.parseDouble(money)*100;
        String o= String.valueOf(mapbody.get("orderid"));
        m=m.substring(0,m.indexOf("."));
        log.info("new钱:"+mo);
        log.info("old钱:"+money1);
        if(mo==money1){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }


}
