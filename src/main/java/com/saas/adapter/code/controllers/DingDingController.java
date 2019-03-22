package com.saas.adapter.code.controllers;


import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.entity.DingDingEntity;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

@RestController
@RequestMapping("/dingding")
@Slf4j
public class DingDingController {

    @Autowired
    RestTemplate template;
    @Autowired
    OrderReturnMain orderReturnMain;
    @Autowired
    private TokenClient tokenClient;
    @Autowired
    public SaasNotifyParams saasNotifyParams;

    @PostMapping("/pay")
    public OrderReturn dingding(@RequestBody Parameter parameter) throws Exception {
        String url = "http://meccus.cn/gateway/index/checkpoint.do";
        String content_type = "json";
        String account_id = "10018";
        String thoroughfare = "dingding_auto";
        String out_trade_no = parameter.order.no;
        log.info(out_trade_no);
        String robin = "2";
        String callback_url = "http://47.106.223.127:8983/dingding/notify";
        String success_url = "http://47.106.223.127:8983/dingding/notify";
        String error_url = "http://47.106.223.127:8983/dingding/notify";
        String s=changeF2Y(parameter.order.money);
        String amount = s;
        log.info(amount);
        String s_key = "D0540592566D2C";
        String url1 = "content_type=" + content_type;
        url1 += "&account_id=" + account_id;
        url1 += "&thoroughfare=" + thoroughfare;
        url1 += "&out_trade_no=" + out_trade_no;
        url1 += "&robin=" + robin;
        url1 += "&callback_url=" + callback_url;
        url1 += "&success_url=" + success_url;
        url1 += "&error_url=" + error_url;
        url1 += "&amount=" + amount;
        url1 += "&s_key=" + s_key;
        // log.info(url1);
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL rul = new URL(url);
            URLConnection conn = rul.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(url1);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            Gson gson = new Gson();
            Map<?, ?> map = gson.fromJson(result, Map.class);
            Map<?, ?> order = (Map<?, ?>) map.get("data");
            String code = String.valueOf(order.get("order_id"));
            String string = "http://meccus.cn/gateway/pay/automaticdingding.do?id=" + code;
            log.info(string);
            tokenClient.savePayUrl(parameter.order.no, parameter.order.orderConfig.notifyUrl, 50);
            return orderReturnMain.successReturn(string, "钉钉支付", parameter.order.money);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @PostMapping("/notify")
    public String dd(DingDingEntity entity){
        String orderid=entity.getOut_trade_no();
        String amount=entity.getAmount();
        log.info(orderid);
        log.info(amount);
        String notifyUrl = tokenClient.getPayParams(orderid);
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, entity, String.class);
        }
        if (entity.getStatus().equals("success")) {
            return "success";
        }
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
        if(money.contains(".")){
            money=money.substring(0,money.indexOf("."));
        }
        money=changeF2Y(money);

        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("amount"));
        String o= String.valueOf(mapbody.get("out_trade_no"));
        log.info("结果");
//        String m=str.substring(str.indexOf("amount")+11,str.indexOf("out_trade_no")-5);
        log.info("钱"+m);
//        String o=str.substring(str.indexOf("out_trade_no")+17,str.indexOf("trade_no")-5);

        log.info("old钱"+money);
        if(m.equals(money)){
            return saasNotifyParams.successParams(o, "success");
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
