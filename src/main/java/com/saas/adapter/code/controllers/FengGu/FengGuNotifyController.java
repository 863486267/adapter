package com.saas.adapter.code.controllers.FengGu;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.saas.adapter.tools.MD5.changeF2Y;

@RestController
@Slf4j
public class FengGuNotifyController {


    @Autowired
    RestTemplate template;
    @Autowired
    private TokenClient tokenClient;
    @Autowired
    public OrderReturnMain orderReturnMain;
    @Autowired
    public SaasNotifyParams saasNotifyParams;


    Map<String,String> maps=new ConcurrentHashMap<>();
    @PostMapping("/Pay_Gao_xintiao.html")
    public void pay(@RequestBody String s){
        log.info("Pay_Gao_xintiao:"+s);
    }

    @PostMapping("/zfbpayzd/notify")
    public void pays(@RequestBody String zfbpayzd){
       // String zfbpayzd="payurl=20190320200040011100810021969764&mark=1223933725&money=0.01&type=alipay&key=fdfdagfdshgfdhgfdhgdfs";
        log.info("zfbpayzd:"+zfbpayzd);
        Map<String,String > map=new HashMap<>();
        String [] arr=zfbpayzd.split("&");
        for(int i=0;i<arr.length;i++){
            System.out.println(arr[i]);
            String [] str=arr[i].split("=");
            if(str.length==2){
                map.put(str[0],str[1]);
            }
        }
        JSONObject object= JSONObject.parseObject(JSON.toJSONString(map));
        log.info(String.valueOf(object));

        String url="alipays://platformapi/startapp?appId=20000090&actionType=toBillDetails&tradeNO="+map.get("payurl");
        maps.put(map.get("mark"),url);
    }

    @GetMapping("/getData")
    public String getdata(String orderId){
        log.info("orderId:"+orderId);
        log.info("URL:"+maps.get(orderId));
        String url=maps.get(orderId);
        maps.remove(orderId);
        return url;
    }



    @PostMapping("/zfbpay/notify")
    public String payss(@RequestBody String zfbpay){
        //String  zfbpay="dt=1553059285101&no=20190320200040011100810021969764&money=0.01&id=&order=1223933725&key=31fa84bf01f60522548cce7a22d8ec15&today_money=&today_pens=";
        log.info("zfbpay:"+zfbpay);
        Map<String,String > map=new HashMap<>();
        String [] arr=zfbpay.split("&");
        for(int i=0;i<arr.length;i++){
            System.out.println(arr[i]);
            String [] s=arr[i].split("=");
            if(s.length==2){
                map.put(s[0],s[1]);
            }
        }
        JSONObject object= JSONObject.parseObject(JSON.toJSONString(map));
        log.info(String.valueOf(object));

        String ordermoney=map.get("money");
        String orderid=map.get("order");
        log.info("ordermoney"+ordermoney);
        log.info("orderid"+orderid);
        FengGuEntity entity=new FengGuEntity();
        entity.setOrderid(orderid);
        entity.setOrdermoney(ordermoney);

        String notifyUrl = tokenClient.getPayParams(orderid);
        log.info("SAAS回调地址:"+notifyUrl);
        if(notifyUrl!=null){
            template.postForObject(notifyUrl, entity, String.class);
            return "处理成功";
        }else {
            return "fail";
        }

    }










    @PostMapping("/fenggu/notifys")
    public CallbackResult suinotifyurl(@RequestBody String str) throws Exception {
        log.info(str);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        Gson gson = new Gson();
        Map<?, ?> map = gson.fromJson(str, Map.class);
        Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
        String money = String.valueOf(orderMap.get("money"));
        log.info("m1:"+money);
        if(money.contains(".")){
            money=money.substring(0,money.indexOf("."));
        }
        money=changeF2Y(money);
        if(money.contains(".")){
            money=money.substring(0,money.indexOf("."));
        }

        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("ordermoney"));
        log.info("m2:"+m);
        if(m.contains(".")){
            m=m.substring(0,m.indexOf("."));
        }
        String o= String.valueOf(mapbody.get("orderid"));
        log.info("o:"+o);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }

}
