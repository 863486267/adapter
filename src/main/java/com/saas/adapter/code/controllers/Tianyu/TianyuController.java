package com.saas.adapter.code.controllers.Tianyu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.saas.adapter.clients.TokenClient;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.saas.adapter.code.controllers.Tianyu.EntityKeyValue.*;


@RestController
@Slf4j
@RequestMapping("/tianyu")
public class TianyuController {

    RestTemplate template = new RestTemplate();
    Map<String, String> htmlTextMap = new ConcurrentHashMap<>();

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

        /**
         * 支付宝支付
         */
        Integer pay_method_alipay = 300;
        /**
         * 微信支付
         */
        Integer client_type_json = 2;
        /**
         * 测试地址
         */
        String login_name = "18220029457";
        String login_key = "cc5da2b7c484ffdef717642a";
        String urlString = "http://pay.tianyu768.com/face/MerchantInterface/create_payment_record.do";
        String requestFlow = parameter.order.no;
        String price_fen = parameter.order.money;
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("body", "支付");
        jsonObject.addProperty("attach", "商品");
        jsonObject.addProperty("requestFlow", requestFlow);
        jsonObject.addProperty("price_fen", price_fen);
        jsonObject.addProperty("pay_method", "" + pay_method_alipay);

        String randCode = parameter.order.no;
        log.info(price_fen);
        log.info("randCode:"+randCode);
        String sign_only = "true";
        String client_type = client_type_json + "";

        String data = jsonObject.toString();

        String tempKey = MD5Normal(login_key + randCode);

        String mac_old = null;
        try {
            mac_old = tempKey + StringToHex(login_name) + data;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String mac = MD5Normal(mac_old);

        List<EntityKeyValue> entityKeyValues = new LinkedList();

        entityKeyValues.add(new EntityKeyValue("username", login_name));
        entityKeyValues.add(new EntityKeyValue("timestamp", randCode));
        entityKeyValues.add(new EntityKeyValue("sign_only", sign_only));
        entityKeyValues.add(new EntityKeyValue("client_type", client_type));
        entityKeyValues.add(new EntityKeyValue("data", data));
        entityKeyValues.add(new EntityKeyValue("mac", mac));

        String result = null;
        try {
            result = http_post(urlString, entityKeyValues);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject jsonObject_root = parseJsonObject(result);

        JsonObject jsonObject_data = getJsonObject(jsonObject_root, "data");

        String pay_data = getString(jsonObject_data, "pay_data");

        JsonObject jsonObject_pay_data = parseJsonObject(pay_data);

        String redirect_url = getString(jsonObject_pay_data, "redirect_url");
//
//        JsonObject Jdata = getJsonObject(jsonObject_pay_data, "redirect_parameter");
//        String pay_memberid = getString(Jdata, "pay_memberid");
//        String pay_orderid = getString(Jdata, "pay_orderid");
//        String pay_applydate = getString(Jdata, "pay_applydate");
//        String pay_bankcode = getString(Jdata, "pay_bankcode");
//        String pay_callbackurl = getString(Jdata, "pay_callbackurl");
//        String pay_notifyurl = getString(Jdata, "pay_notifyurl");
//        String pay_amount = getString(Jdata, "pay_amount");
//        String pay_productname = getString(Jdata, "pay_productname");
//        String pay_md5sign = getString(Jdata, "pay_md5sign");
//        String pay_productnum = getString(Jdata, "pay_productnum");
//        String pay_productdesc = getString(Jdata, "pay_productdesc");
//        String pay_producturl = getString(Jdata, "pay_producturl");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
//        map.add("pay_memberid", pay_memberid);
//        map.add("pay_orderid", pay_orderid);
//        map.add("pay_applydate", pay_applydate);
//        map.add("pay_bankcode", pay_bankcode);
//        map.add("pay_callbackurl", pay_callbackurl);
//        map.add("pay_notifyurl", pay_notifyurl);
//        map.add("pay_amount", pay_amount);
//        map.add("pay_productname", pay_productname);
//        map.add("pay_md5sign", pay_md5sign);
//        map.add("pay_productnum", pay_productnum);
//        map.add("pay_productdesc", pay_productdesc);
//        map.add("pay_producturl", pay_producturl);
//        HttpEntity<MultiValueMap> entity = new HttpEntity<>(map, headers);
//        ResponseEntity response = template.exchange(redirect_url, HttpMethod.POST, entity, String.class);
//        String url = String.valueOf(response);
//        url = url.substring(url.indexOf("<!DOCTYPE"), url.indexOf("</html>") + 7);
//        htmlTextMap.put(pay_orderid,url);
//        log.info("url:" + url);
//        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
//        String saas="http://47.106.223.127:8984/tianyu/payment?orderid="+pay_orderid;


        tokenClient.savePayUrl(requestFlow, parameter.order.orderConfig.notifyUrl, 50);
        log.info(redirect_url);
        return orderReturnMain.successReturn(redirect_url, "充值金额", parameter.order.money);
    }

//    @GetMapping("/payment")
//    public String payment(String orderid) {
//        String htmlText = htmlTextMap.get(orderid).toString();
//        log.info(htmlText);
//        htmlTextMap.remove(orderid);
//        return htmlText;
//    }


    @PostMapping("/notify")
    public String not(@RequestBody String  string){
        String[]  arrayString=string.split("&");
        Map map=new HashMap();
        for (String arr:arrayString) {
            String[] keyvalue=arr.split("=");
            if(keyvalue.length==2){
                map.put(keyvalue[0],keyvalue[1]);
            }

        }
        log.info(JSONObject.parseObject(JSON.toJSONString(map)).toJSONString());
//        Gson gson = new Gson();
//        Map<?, ?> map = gson.fromJson(string, Map.class);
//        String businessIntId=String.valueOf(map.get("businessIntId"));
//        String id=String.valueOf(map.get("id"));
//        String payFlow=String.valueOf(map.get("payFlow"));
//        String payTime=String.valueOf(map.get("payTime"));
//        String pay_method=String.valueOf(map.get("pay_method"));
//        String price_fen_all=String.valueOf(map.get("price_fen_all"));
//        String requestFlow=String.valueOf(map.get("requestFlow"));
//        String state=String.valueOf(map.get("state"));
//        String userId_huiyuan=String.valueOf(map.get("userId_huiyuan"));
//        String userId_merchant=String.valueOf(map.get("userId_merchant"));
//        String sign=String.valueOf(map.get("sign"));
        String payFlow= String.valueOf(map.get("payFlow"));
        String price_fen_all= String.valueOf(map.get("price_fen_all"));
        String requestFlow= String.valueOf(map.get("requestFlow"));
        String state= String.valueOf(map.get("state"));
        log.info("payFlow"+payFlow);
        log.info("monsys:"+price_fen_all);
        TianyuEntity entitys=new TianyuEntity();
        entitys.setPrice_fen_all(price_fen_all);
        entitys.setRequestFlow(requestFlow);

//        String s="businessIntId="+businessIntId+"&id="+id+"&pay_method="+pay_method+"&payFlow="+payFlow+"&payTime="+payTime
//                +"&price_fen_all="+price_fen_all+"&requestFlow="+requestFlow+"&state="+state+"&userId_huiyuan="+userId_huiyuan
//                +"&userId_merchant="+userId_merchant+"&cc5da2b7c484ffdef717642a";

//        String signnew=MD5.md5(s);
//        log.info("sign:"+sign);
//        log.info("signnew:"+signnew);
        String notifyUrl = tokenClient.getPayParams(requestFlow);
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, entitys, String.class);
        }
//        if(sign.equals(signnew)){
            if(state.equals("207")){
                return "success";
            }
            return "failure";
//        }
//        return "error";
    }



    @PostMapping("/notifys")
    public CallbackResult suinotifyurl(@RequestBody String str) throws Exception {
        log.info(str);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        //businessIntId=30000&id=5c90cf05d7cd4e2b87a6b1e0&payFlow=&payTime=1552994096921&pay_method=%E6%94%AF%E4%BB%98%E5%AE%9D%E6%94%AF%E4%BB%98&price_fen_all=-1&requestFlow=201903191914136992196148502&state=207&userId_huiyuan=&userId_merchant=5c8dcbead7cd4e223f7bb368&sign=d3a9f215c9ca5232d3738e0f36841294

        Gson gson = new Gson();
        Map<?, ?> map = gson.fromJson(str, Map.class);
        Map<?, ?> orderMap = (Map<?, ?>) map.get("order");
        String money = String.valueOf(orderMap.get("money"));
        log.info("money:"+money);
        if(money.contains(".")){
            money=money.substring(0,money.indexOf("."));
        }
        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("price_fen_all"));
        String o= String.valueOf(mapbody.get("requestFlow"));
        if(m.contains("-")){
            m=m.replaceAll("-","");
        }
        log.info("m:"+m);
        if(m.contains(".")){
            m=m.substring(0,m.indexOf("."));
        }
        log.info("new钱:"+m);
        log.info("old钱:"+money);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }



}
