package com.saas.adapter.code.controllers;

import com.google.gson.Gson;
import com.saas.adapter.clients.TokenClient;
import com.saas.adapter.entity.HaoETongEntity;
import com.saas.adapter.entity.JiaLianEntity;
import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;
import com.saas.adapter.tools.MD5;
import com.saas.adapter.tools.OrderReturnMain;
import com.saas.adapter.tools.SaasNotifyParams;
import com.saas.adapter.tools.tool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/haoetong")
@Slf4j
public class HaoETong {

    @Autowired
    RestTemplate template;

    @Autowired
    private TokenClient tokenClient;
    @Autowired
    public OrderReturnMain orderReturnMain;
    @Autowired
    public SaasNotifyParams saasNotifyParams;

    Map htmlTextMap = new HashMap();

    @PostMapping("/pay")
    public OrderReturn pay(@RequestBody Parameter parameter) throws Exception {

        String url="http://www.manhaoee.cn/gateway";//接口路径
        String key= "ca794b63bcec6d7cd7bd1f8dec707e8a5f23757d";//接入密钥 不传参
        String version="1.0";//版本号 固定值1.0
        String customerid="6547";//商户号
        String total_fee=MD5.changeF2Y(parameter.order.money);//金额必须是XX.00
        log.info(total_fee);
        if(!total_fee.contains(".")){
            total_fee=total_fee+".00";
        }
        log.info(total_fee);
        String paytype="alipay";//支付类型:支付宝扫码-alipay、财付通-tenpay、微信扫码-weixin、微信公众号-gzhpay、微信H5-wxh5、在线网银-bank
        //String bankcode="CMB";//paytype为bank时，bankcode为以下银行取值ICBC、ABC、CMD.........
        String notifyurl="http://47.106.223.127:8983/haoetong/notify";//异步通知URL
        String returnurl="http://47.106.223.127:8983/haoetong/notify";//同步跳转URL
        String sdorderno= parameter.order.no;//商户订单号
        log.info(sdorderno);
        String sign="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&"+key;//MD5加密 小写
        sign= tool.getMD5(sign);
        log.info(sign);
        String parms="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&paytype="+paytype+"&bankcode=ICBC&sign="+sign;
        PrintWriter out=null;
        BufferedReader in=null;
        String  result="";
        try
        {
            URL rul=new URL(url);
            URLConnection conn=rul.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out= new PrintWriter(conn.getOutputStream());
            out.print(parms);
            out.flush();
            in= new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while((line=in.readLine())!=null){
                result+=line;
            }
            String htmlText=String.valueOf(result);
            htmlTextMap.put(sdorderno,htmlText);
            log.info(htmlText);
            tokenClient.savePayUrl(sdorderno, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8983/ningmeng/obtain?htmlText="+htmlText;
            return orderReturnMain.successReturn("http://47.106.223.127:8983/haoetong/payment?orderid="+sdorderno, "充值金额", parameter.order.money);
        } catch (Exception e)
        {
            e.printStackTrace();
        }finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    @PostMapping("/paywap")
    public OrderReturn pay_wap(@RequestBody Parameter parameter) throws Exception {

        String url="http://www.manhaoee.cn/gateway";//接口路径
        String key= "ca794b63bcec6d7cd7bd1f8dec707e8a5f23757d";//接入密钥 不传参
        String version="1.0";//版本号 固定值1.0
        String customerid="6547";//商户号
        String total_fee=MD5.changeF2Y(parameter.order.money);//金额必须是XX.00
        log.info(total_fee);
        if(!total_fee.contains(".")){
            total_fee=total_fee+".00";
        }
        log.info(total_fee);
        String paytype="alipaywap";//支付类型:支付宝扫码-alipay、财付通-tenpay、微信扫码-weixin、微信公众号-gzhpay、微信H5-wxh5、在线网银-bank
        //String bankcode="CMB";//paytype为bank时，bankcode为以下银行取值ICBC、ABC、CMD.........
        String notifyurl="http://47.106.223.127:8983/haoetong/notify";//异步通知URL
        String returnurl="http://47.106.223.127:8983/haoetong/notify";//同步跳转URL
        String sdorderno= parameter.order.no;//商户订单号
        log.info(sdorderno);
        String sign="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&"+key;//MD5加密 小写
        sign= tool.getMD5(sign);
        log.info(sign);
        String parms="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&paytype="+paytype+"&bankcode=ICBC&sign="+sign;
        PrintWriter out=null;
        BufferedReader in=null;
        String  result="";
        try
        {
            URL rul=new URL(url);
            URLConnection conn=rul.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out= new PrintWriter(conn.getOutputStream());
            out.print(parms);
            out.flush();
            in= new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while((line=in.readLine())!=null){
                result+=line;
            }
            String htmlText=String.valueOf(result);
            htmlTextMap.put(sdorderno,htmlText);
            log.info(htmlText);
            tokenClient.savePayUrl(sdorderno, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8983/ningmeng/obtain?htmlText="+htmlText;
            return orderReturnMain.successReturn("http://47.106.223.127:8983/haoetong/payment?orderid="+sdorderno, "充值金额", parameter.order.money);
        } catch (Exception e)
        {
            e.printStackTrace();
        }finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }


    @PostMapping("/paywechat")
    public OrderReturn pay_wechat(@RequestBody Parameter parameter) throws Exception {

        String url="http://www.manhaoee.cn/gateway";//接口路径
        String key= "ca794b63bcec6d7cd7bd1f8dec707e8a5f23757d";//接入密钥 不传参
        String version="1.0";//版本号 固定值1.0
        String customerid="6547";//商户号
        String total_fee=MD5.changeF2Y(parameter.order.money);//金额必须是XX.00
        log.info(total_fee);
        if(!total_fee.contains(".")){
            total_fee=total_fee+".00";
        }
        log.info(total_fee);
        String paytype="weixin";//支付类型:支付宝扫码-alipay、财付通-tenpay、微信扫码-weixin、微信公众号-gzhpay、微信H5-wxh5、在线网银-bank
        //String bankcode="CMB";//paytype为bank时，bankcode为以下银行取值ICBC、ABC、CMD.........
        String notifyurl="http://47.106.223.127:8983/haoetong/notify";//异步通知URL
        String returnurl="http://47.106.223.127:8983/haoetong/notify";//同步跳转URL
        String sdorderno= parameter.order.no;//商户订单号
        log.info(sdorderno);
        String sign="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&"+key;//MD5加密 小写
        sign= tool.getMD5(sign);
        log.info(sign);
        String parms="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&paytype="+paytype+"&bankcode=ICBC&sign="+sign;
        PrintWriter out=null;
        BufferedReader in=null;
        String  result="";
        try
        {
            URL rul=new URL(url);
            URLConnection conn=rul.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out= new PrintWriter(conn.getOutputStream());
            out.print(parms);
            out.flush();
            in= new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while((line=in.readLine())!=null){
                result+=line;
            }
            String htmlText=String.valueOf(result);
            htmlTextMap.put(sdorderno,htmlText);
            log.info(htmlText);
            tokenClient.savePayUrl(sdorderno, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8983/ningmeng/obtain?htmlText="+htmlText;
            return orderReturnMain.successReturn("http://47.106.223.127:8983/haoetong/payment?orderid="+sdorderno, "充值金额", parameter.order.money);
        } catch (Exception e)
        {
            e.printStackTrace();
        }finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }

    @PostMapping("/paywechatH5")
    public OrderReturn pay_wechatH5(@RequestBody Parameter parameter) throws Exception {

        String url="http://www.manhaoee.cn/gateway";//接口路径
        String key= "ca794b63bcec6d7cd7bd1f8dec707e8a5f23757d";//接入密钥 不传参
        String version="1.0";//版本号 固定值1.0
        String customerid="6547";//商户号
        String total_fee=MD5.changeF2Y(parameter.order.money);//金额必须是XX.00
        log.info(total_fee);
        if(!total_fee.contains(".")){
            total_fee=total_fee+".00";
        }
        log.info(total_fee);
        String paytype="wxh5";//支付类型:支付宝扫码-alipay、财付通-tenpay、微信扫码-weixin、微信公众号-gzhpay、微信H5-wxh5、在线网银-bank
        //String bankcode="CMB";//paytype为bank时，bankcode为以下银行取值ICBC、ABC、CMD.........
        String notifyurl="http://47.106.223.127:8983/haoetong/notify";//异步通知URL
        String returnurl="http://47.106.223.127:8983/haoetong/notify";//同步跳转URL
        String sdorderno= parameter.order.no;//商户订单号
        log.info(sdorderno);
        String sign="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&"+key;//MD5加密 小写
        sign= tool.getMD5(sign);
        log.info(sign);
        String parms="version="+version+"&customerid="+customerid+"&total_fee="+total_fee+"&sdorderno="+sdorderno+"&notifyurl="+notifyurl+"&returnurl="+returnurl+"&paytype="+paytype+"&bankcode=ICBC&sign="+sign;
        PrintWriter out=null;
        BufferedReader in=null;
        String  result="";
        try
        {
            URL rul=new URL(url);
            URLConnection conn=rul.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out= new PrintWriter(conn.getOutputStream());
            out.print(parms);
            out.flush();
            in= new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while((line=in.readLine())!=null){
                result+=line;
            }
            String htmlText=String.valueOf(result);
            htmlTextMap.put(sdorderno,htmlText);
            log.info(htmlText);
            tokenClient.savePayUrl(sdorderno, parameter.order.orderConfig.notifyUrl, 50);
//        return "http://47.106.220.74:8983/ningmeng/obtain?htmlText="+htmlText;
            return orderReturnMain.successReturn("http://47.106.223.127:8983/haoetong/payment?orderid="+sdorderno, "充值金额", parameter.order.money);
        } catch (Exception e)
        {
            e.printStackTrace();
        }finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }


    @GetMapping("/payment")
    public String payment(String orderid){
        String htmlText=htmlTextMap.get(orderid).toString();
        htmlTextMap.remove(orderid);
        return htmlText;
    }


    @PostMapping("/notify")
    public String dd(HaoETongEntity entity){
        String sdorderno=entity.getSdorderno();
        String total_fee=entity.getTotal_fee();
        int status=entity.getStatus();
        log.info(sdorderno);
        log.info(total_fee);
        int customerid=entity.getCustomerid();
        String sdpayno=entity.getSdpayno();
        String paytype=entity.getPaytype();
        String sign=entity.getSign();

        String s="customerid="+customerid+"&status="+status+"&sdpayno="+sdpayno+"&sdorderno="+sdorderno+"&total_fee="+total_fee+"&paytype="+paytype+"&ca794b63bcec6d7cd7bd1f8dec707e8a5f23757d";
        String signnew=tool.getMD5(s);

        String notifyUrl = tokenClient.getPayParams(sdorderno);
        log.info("回调地址2"+notifyUrl);
        if(!(notifyUrl==null)) {
            template.postForObject(notifyUrl, entity, String.class);
        }
        log.info("三方："+sign);
        log.info("三方："+entity.getSign());
        if(sign.equals(signnew)) {
            if (status==1) {
                log.info("success");
                return "success";
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
        money=MD5.changeF2Y(money);
        Map<?, ?> paramMap = (Map<?, ?>) map.get("param");
        String body = String.valueOf(paramMap.get("body"));
        Map<?, ?> mapbody = gson.fromJson(body, Map.class);
        String m= String.valueOf(mapbody.get("total_fee"));
        String o= String.valueOf(mapbody.get("sdpayno"));
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

}
