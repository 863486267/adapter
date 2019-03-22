package com.saas.adapter.code.controllers.chaofan;

import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.code.controllers.VVYY.PayUtil;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.DateUtil;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;

import static com.saas.adapter.tools.MD5.changeF2Y;

/**
 * @author chuanjieyang
 * @since Mar 22, 2019 12:12:38 PM
 */
@RestController("chaoFanPayController")
@RequestMapping("/chaofan")
@Slf4j
@ConfigurationProperties(prefix = "ip.port")
@Data
@ToString
public class PayController {

    private String notify;
    public static final String chaofan_key = "cc829072eb2e171ab1a69c695fdc86a2";
    public static final String chaofan_pay_url = "https://pay.cfanpay.com/gateway";
    public static final String merchantId = "190032";

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
     * 支付宝扫码
     * @param parameter
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/pay")
    public OrderReturn pay(@RequestBody Parameter parameter,HttpServletRequest request) throws Exception {
        String orderNo = parameter.order.no;
        String amount = parameter.order.money;
        String clientIp=getClientIp(request);
        String tradeDate= DateUtil.generateTime("yyyyMMdd");
        String tradeTime= DateUtil.generateTime("HHmmss");
        log.info("订单号：{}",orderNo);
        log.info("金额：{}",amount);
        log.info("客户端ip：{}",clientIp);
        log.info("交易日期：{}",tradeDate);
        log.info("交易时间：{}",tradeTime);

        Map<String, Object> map = new TreeMap();
        map.put("service", "pay.alipay.qrcode");
        /*
            version 1.0：resultUrl返回一个有二维码支付页面的地址
            version 1.1：resultUrl返回地址由商户自定义去生成二维码后展示出来进行扫码支付
         */
        map.put("version", "1.0");
        map.put("merchantId", merchantId);
        map.put("orderNo", orderNo);
        map.put("tradeDate", tradeDate);
        map.put("tradeTime", tradeTime);
        map.put("amount", amount);
        map.put("clientIp", clientIp);
        map.put("notifyUrl", notify+"/chaofan/notify");
        log.info("notify"+notify);
        String sign = createSign(map, chaofan_key);
        log.info("签名：{}",sign);
        map.put("sign", sign);

        String postResult = PayUtil.executePost(chaofan_pay_url, map);
        log.info("下单结果：{}",postResult);
        Map<String, String> map1 = SignUtils.parseQuery(postResult);
        String resultUrl = map1.get("resultUrl");
        log.info("下单结果：{}",postResult);
        log.info("二维码：{}",resultUrl);


        tokenClient.savePayUrl(orderNo, parameter.order.orderConfig.notifyUrl, 50);


        return orderReturnMain.successReturn(resultUrl, "充值金额", parameter.order.money);
    }


    /**
     * 回调地址
     * @param notifyVO
     * @return
     */
    @PostMapping("/notify")
    public Object payNotify(NotifyVO notifyVO){

        String orderNo=notifyVO.getOrderNo();

        String resultCode=notifyVO.getResultCode();

        Map<String, String> map = new TreeMap();
        map.put("version", notifyVO.getVersion());
        map.put("merchantId", notifyVO.getMerchantId());
        map.put("orderNo", orderNo);
        map.put("tradeDate", notifyVO.getTradeDate());
        map.put("tradeTime", notifyVO.getTradeTime());
        map.put("resultCode",resultCode);
        map.put("amount", notifyVO.getAmount());
        map.put("sign", notifyVO.getSign());

        String notifyUrl = tokenClient.getPayParams(orderNo);
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, notifyVO, String.class);
        }

        boolean result = SignUtils.checkParam(map, chaofan_key);
        if (result){
            if(resultCode.equals("0")){
                //支付成功，写返回数据逻辑
                return "OK";
            }else{
                return"支付失败";
            }
        }else{
            return"验签失败";
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
        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("amount"));
        String o= String.valueOf(mapbody.get("orderNo"));
        if(m.contains(".")){
            m=m.substring(0,m.indexOf("."));
        }
        if(money.contains(".")){
            money=money.substring(0,money.indexOf("."));
        }
        log.info("new钱:"+m);
        log.info("old钱:"+money);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
        }
        return null;
    }





    private static String createSign(Map map, String key) {
        Map<String, String> params = SignUtils.paraFilter(map);
        params.put("key", key);
        StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
        SignUtils.buildPayParams(buf, params, false);
        String preStr = buf.toString();
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
