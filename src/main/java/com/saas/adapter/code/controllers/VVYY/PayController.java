package com.saas.adapter.code.controllers.VVYY;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.DateUtil;
import com.saas.adapter.tools.MD5;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * vvyypay 代付端
 * @author chuanjieyang
 * @since Mar 20, 2019 16:28:55 PM
 */
@RestController("vvvyyPayController")
@RequestMapping("/vvyy")
@Slf4j
public class PayController {


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


    /**
     * 下单
     * @param
     * @return
     */
    @PostMapping("/pay")
    public OrderReturn payOrder(@RequestBody Parameter parameter) throws Exception {
        String pay_memberid = PayUtil.pay_memberid;
        String pay_applydate =  DateUtil.generateTime("yyyy-MM-dd HH:mm:ss");
        String pay_bankcode =  "903";
        String pay_notifyurl =  "http://47.106.223.127:8983/vvyy/notify";
        String pay_callbackurl =  "http://47.106.223.127:8983/vvyy/notify";
        String pay_amount = MD5.changeF2Y(parameter.order.money);
        String pay_orderid=parameter.order.no;
        log.info("SAAS:"+pay_orderid);
        Map<String, Object> map = new HashMap<>();
            map.put("pay_memberid",pay_memberid);
            map.put("pay_orderid",parameter.order.no);
            map.put("pay_applydate", pay_applydate);
            map.put("pay_bankcode", "903");
            map.put("pay_notifyurl", pay_notifyurl);
            map.put("pay_callbackurl", pay_callbackurl);
            map.put("pay_amount", pay_amount);
            map.put("pay_md5sign", PayUtil.getSign(pay_amount,pay_bankcode,pay_callbackurl,pay_memberid,pay_notifyurl,pay_orderid,pay_applydate));
            map.put("pay_productname","充值");
        String postResult = PayUtil.executePost(PayUtil.pay_url, map);
        JSONObject json=JSONObject.parseObject(postResult);
        log.info(json.toJSONString());
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
        if(String.valueOf(json.get("status")).equals("success")){
            String url= String.valueOf(json.get("url"));
            return orderReturnMain.successReturn(url, "充值金额", parameter.order.money);
        }
        return orderReturnMain.failReturn("下单失败！");
    }

    /**
     * 回调地址
     * @return
     */
    @PostMapping("/notify")
    public String payNotify(VVYY entity){
        String memberid=entity.getMemberid();
        String orderid=entity.getOrderid();
        String amount=entity.getAmount();
        String datetime=entity.getDatetime();
        String returncode=entity.getReturncode();
        String transaction_id=entity.getTransaction_id();
        String attach=entity.getAttach();
        String sign=entity.getSign();

        String SignTemp="amount="+amount+"&datetime="+datetime+"&memberid="+memberid+"&orderid="+orderid+"&returncode="+returncode+"&transaction_id="+transaction_id+"&key="+ PayUtil.key +"";
        log.info("SignTemp:"+SignTemp);
        log.info("回调订单号:",orderid);
        String notifyUrl = tokenClient.getPayParams(orderid);
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, entity, String.class);
        }
        String md5sign= PayUtil.getMd5(SignTemp).toUpperCase();//MD5加密
        if (sign.equals(md5sign)){
            if(returncode.equals("00")){
                //支付成功，写返回数据逻辑
                log.info("OK");
                return "OK";
            }else{
                log.info("支付失败");
                return"支付失败";
            }
        }else{
            log.info("验签失败");
            return"验签失败";
        }
    }


    /**
     * sass请求
     * @param str
     * @return
     * @throws Exception
     */
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
        money= MD5.changeF2Y(money);
        if(money.contains(".")){
            money=money.substring(0,money.indexOf("."));
        }
        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("amount"));
        String o= String.valueOf(mapbody.get("orderid"));
        if(m.contains(".")){
            m=m.substring(0,m.indexOf("."));
        }
        log.info(o);
        log.info("new钱:"+m);
        log.info("old钱:"+money);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }



   /* *//**
     * 页面回调
     * @param request
     * @return
     *//*
    @PostMapping("/callback")
    public Object payCallback(HttpServletRequest request){
        return PayUtil.validateSign(request);
    }


    *//**
     * 订单状态查询接口
     * @param pay_memberid
     * @param pay_orderid
     * @param pay_md5sign
     * @return
     *//*
    @GetMapping("/getOrder")
    public Object getOrder(@RequestParam("pay_memberid")String pay_memberid,
                           @RequestParam("pay_orderid")String pay_orderid,
                           @RequestParam(value = "pay_md5sign",required = false)String pay_md5sign){
        return payService.getOrder(pay_memberid,pay_orderid,pay_md5sign);
    }


    *//**
     * 代付
     * @param
     * @return
     *//*
    @PostMapping("/payForAnother")
    public Object payForAnother(*//*PayAnotherVo payAnotherVo*//*){
        PayAnotherVo payAnotherVo = new PayAnotherVo();
        return payService.payForAnother(payAnotherVo);
    }

    *//**
     * 代付结果查询
     * @param out_trade_no
     * @param mchid
     * @param pay_md5sign
     * @return
     *//*
    @GetMapping("/getPayForAnotherResult")
    public Object getPayForAnotherResult(@RequestParam("out_trade_no")String out_trade_no,
                                         @RequestParam("mchid")String mchid,
                                         @RequestParam(value = "pay_md5sign",required = false)String pay_md5sign){
        return payService.getOrder(out_trade_no,mchid,pay_md5sign);
    }*/
}
