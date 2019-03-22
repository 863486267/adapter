package com.saas.adapter.code.controllers.VVYY;

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

    Map htmlTextMap = new HashMap();

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
        String pay_notifyurl =  "http://47.106.223.127:8984/vvyy/notify";
        String pay_callbackurl =  "http://47.106.223.127:8984/vvyy/notify";
        String pay_amount = MD5.changeF2Y(parameter.order.money);
        String pay_orderid=parameter.order.no;
        Map<String, Object> map = new HashMap<>();
            map.put("pay_memberid",pay_memberid);
            map.put("pay_orderid",parameter.order.no);
            map.put("pay_applydate", pay_applydate);
            map.put("pay_bankcode", "903");
            map.put("pay_notifyurl", pay_notifyurl);
            map.put("pay_callbackurl", pay_callbackurl);
            map.put("pay_amount", pay_amount);
            map.put("pay_md5sign", PayUtil.getSign(pay_amount,pay_bankcode,pay_callbackurl,pay_memberid,pay_notifyurl,pay_orderid,pay_applydate));
            map.put("pay_productname","100元话费充值");
        String postResult = PayUtil.executePost(PayUtil.pay_url, map);
        String htmlText=String.valueOf(postResult);
        htmlTextMap.put(pay_orderid,htmlText);
        log.info(htmlText);
        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);
        String url="http://47.106.223.127:8984/vvyy/payment?orderid="+pay_orderid;
        return orderReturnMain.successReturn(url, "充值金额", parameter.order.money);
    }

    /**
     * 传给sasa的支付地址
     * @param orderid
     * @return
     */
    @GetMapping("/payment")
    public String payment(String orderid){
        String htmlText=htmlTextMap.get(orderid).toString();
        htmlTextMap.remove(orderid);
        return htmlText;
    }

    /**
     * 回调地址
     * @param request
     * @return
     */
    @PostMapping("/notify")
    public Object payNotify(HttpServletRequest request){
        return PayUtil.validateSign(request);
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
        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("total_fee"));
        String o= String.valueOf(mapbody.get("pay_memberid"));
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
