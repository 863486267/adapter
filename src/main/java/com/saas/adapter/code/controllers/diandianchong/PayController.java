package com.saas.adapter.code.controllers.diandianchong;

import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.controllers.VVYY.PayUtil;
import com.saas.adapter.code.controllers.chaofan.MD5;
import com.saas.adapter.code.controllers.chaofan.SignUtils;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.DateUtil;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.saas.adapter.tools.MD5.changeF2Y;

/**
 * @author chuanjieyang
 * @since Mar 22, 2019 12:12:38 PM
 */
@RestController("huifuPayController")
@RequestMapping("/diandianchong")
@Slf4j
public class PayController {

    public static final String huifu_key = "nrn7aihumy8sl4fbyqps1ipc1ixo8v2g";
    public static final String huifu_pay_url = "http://pay.huifupay.cn/Pay_Index.html";
    public static final String pay_memberid = "190379059";

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
     * 支付宝扫码
     * @param parameter
     * @return
     * @throws Exception
     */
    @PostMapping("/pay")
    public OrderReturn pay(@RequestBody Parameter parameter) throws Exception {
        String pay_orderid = parameter.order.no;
        String pay_amount = changeF2Y(parameter.order.money);
        String pay_applydate= DateUtil.generateTime("yyyy-MM-dd HH:mm:ss");
        log.info("订单号：{}",pay_orderid);
        log.info("金额：{}",pay_amount);
        log.info("交易日期时间：{}",pay_applydate);

        Map<String, Object> map = new TreeMap();
        map.put("pay_memberid", pay_memberid);
        map.put("pay_orderid", pay_orderid);
        map.put("pay_applydate", pay_applydate);
        map.put("pay_bankcode", "903");
        map.put("pay_notifyurl", "http://47.106.223.127:8983/diandianchong/notify");
        map.put("pay_callbackurl", "http://47.106.223.127:8983/diandianchong/notify");
        map.put("pay_amount", pay_amount);

        String sign = createSign(map, huifu_key);
        log.info("签名：{}",sign);
        map.put("pay_productname", "100元话费");
        map.put("pay_md5sign", sign);


        String postResult = PayUtil.executePost(huifu_pay_url, map);
        log.info("下单结果：{}",postResult);

        htmlTextMap.put(pay_orderid,postResult);

        tokenClient.savePayUrl(pay_orderid, parameter.order.orderConfig.notifyUrl, 50);


        String url ="http://47.106.223.127:8983/diandianchong/payment?pay_orderid="+pay_orderid;

        return orderReturnMain.successReturn(url, "充值金额", parameter.order.money);
    }



    @GetMapping("/payment")
    public String payment(String pay_orderid){
        String htmlText=htmlTextMap.get(pay_orderid).toString();
        htmlTextMap.remove(pay_orderid);
        return htmlText;
    }

    /**
     * 回调地址
     * @param huifoVo
     * @return
     */
    @PostMapping("/notify")
    public Object payNotify(HuifoVo huifoVo){

        log.info("开始回调");
        String orderid = huifoVo.getOrderid();

        String returncode = huifoVo.getReturncode();

        Map<String, String> map = new TreeMap();
        map.put("memberid", huifoVo.getMemberid());
        map.put("orderid", orderid);
        map.put("amount", huifoVo.getAmount());

        map.put("transaction_id",huifoVo.getTransaction_id());
        map.put("datetime", huifoVo.getDatetime());
        map.put("returncode",returncode);
        map.put("attach", huifoVo.getAttach());

        map.put("sign", huifoVo.getSign());

        String notifyUrl = tokenClient.getPayParams(orderid);

        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, huifoVo, String.class);
        }

        boolean result = SignUtils.checkParam(map, huifu_key);


            if(returncode.equals("00")){
                //支付成功，写返回数据逻辑
                log.info("OK");
                return "OK";
            }else{
                log.info("支付失败");
                return"支付失败";
            }
        }




    /**
     * sass回调
     * @param str
     * @return
     * @throws Exception
     */
    @PostMapping("/notifys")
    public CallbackResult suinotifyurl(@RequestBody String str) throws Exception {
        log.info(str);
        if (org.apache.commons.lang.StringUtils.isBlank(str)) {
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
        String o= String.valueOf(mapbody.get("orderid"));
        m=m.substring(0,m.indexOf("."));
        log.info("new钱:"+m);
        log.info("old钱:"+money);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return saasNotifyParams.failParams("200","金额不匹配！");
    }





    private static String createSign(Map map, String key)
    {
        Map<String, String> params = SignUtils.paraFilter(map);
        //  params.put("key", key);
        StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
        SignUtils.buildPayParams(buf, params, false);
        String preStr = buf.toString()+"&key=nrn7aihumy8sl4fbyqps1ipc1ixo8v2g";
        String sign = MD5.sign(preStr, "UTF-8");

        return sign;
    }

    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if(index != -1){
                return ip.substring(0,index);
            }else{
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if(StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)){
            return ip;
        }
        return request.getRemoteAddr();
    }
}
