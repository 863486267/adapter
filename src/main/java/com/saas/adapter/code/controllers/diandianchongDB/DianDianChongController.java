package com.saas.adapter.code.controllers.diandianchongDB;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/diandianchong")
@Slf4j

public class DianDianChongController {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private TokenClient tokenClient;

    @Autowired
    public OrderReturnMain orderReturnMain;
    @Autowired
    public SaasNotifyParams saasNotifyParams;

    @GetMapping("/pay")
    public void diandainpay() {


        String url="https://redenvelop.laiwang.com/v2/redenvelop/send/doGenerate";

        String size = "1";
        String appkey="21603258";
        String congratulations = "恭喜发财";
        String amount = "1";
        String _v_ = "3";
        String t = "1553013482919";
        String imei = "111111111111111";
        String imsi = "111111111111111";
        String type = "0";
        String sender = "57084005";
        String access_token = "744e2a0f8d9b3dcb81eab7f35903db5_57084005";

        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String,String> map=new LinkedMultiValueMap<>();
        map.add("size",size);
        map.add("appkey",appkey);
        map.add("congratulations",congratulations);
        map.add("amount",amount);
        map.add("_v_",_v_);
        map.add("t",t);
        map.add("imei",imei);
        map.add("imsi",imsi);
        map.add("type",type);
        map.add("sender",sender);
        map.add("access_token",access_token);

        HttpEntity<MultiValueMap > httpEntity=new HttpEntity<>(map,headers);
        ResponseEntity entity=restTemplate.exchange(url, HttpMethod.POST,httpEntity,String.class);
        JSONObject jsonObject= JSON.parseObject(JSON.toJSONString(entity));
        log.info(String.valueOf(jsonObject));

        Map<?, ?> mapbody = JSON.parseObject(JSON.toJSONString(jsonObject), Map.class);
        String body=String.valueOf(mapbody.get("body"));
        log.info(body);
        JSONObject object=JSON.parseObject(body);
        Map<?,?> tread=JSON.parseObject(JSON.toJSONString(object));
        String businessId= String.valueOf(tread.get("businessId"));

        String urlone="http://api.laiwang.com/v2/internal/act/alipaygift/getPayParams?tradeNo="+businessId+"&bizType=biz_account_transfer&access_token="+access_token;
        log.info(urlone);
        String result = restTemplate.getForObject(urlone, String.class);
        log.info(result);

        Map<String,String> mapres=JSON.parseObject(result,Map.class);
        String urltwo=mapres.get("value");
        log.info(urltwo);



    }



}
