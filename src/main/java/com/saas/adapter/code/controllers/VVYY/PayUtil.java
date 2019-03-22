package com.saas.adapter.code.controllers.VVYY;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * 支付工具常用方法
 * @author chuanjieyang
 * @since Mar 20, 2019 10:12:18 AM
 */
@Slf4j
public class PayUtil {

    public static final String key ="u8qtwz7gj1ovyb14xk6qz83xsy7xjwr4";
    public static final String pay_memberid ="190330628";
    public static final String pay_url ="http://www.vvyypay.com/Pay_Index.html";

    /**
     * md5加密
     * @param key
     * @return
     */
    public static String getMd5(String key){
        StringBuffer stringBuffer = new StringBuffer();
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(key.getBytes("UTF-8"));//向MessageDigest传入一个数据
            byte[] bytes = md5.digest();//计算散列码 长度为16的字节数组
            int i ;
            for (int offest=0;offest<bytes.length;offest++){
                i =bytes[offest];
                if(i<0){
                    i+=256;
                }
                if(i<16)
                    stringBuffer.append(0);
                stringBuffer.append(Integer.toHexString(i));
                //stringBuffer.append(Integer.toHexString(0xFF&bytes[offest]));
            }

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 生成20位订单号 时间戳+6位随机字符串组成
     * 订单号唯一, 字符长度20
     * @return
     */
    public static String generateOrderId(){
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String keyup_prefix= LocalDateTime.now().format(pattern);
        String keyup_append=String.valueOf(new Random().nextInt(899999)+100000);
        return keyup_prefix+keyup_append;//订单号
    }

    /**
     * 生成提交时间
     * 时间格式：2016-12-26 18:18:18
     * @return
     */
    public static String generateTime(){
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(pattern);
    }


    /**
     * 注意：sign加密后字母应转为大写
     * @return
     */
    public static String getSign(String pay_amount,String pay_bankcode,String pay_callbackurl,String pay_memberid,String pay_notifyurl,String pay_orderid,String pay_applydate){

        return  getMd5("pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode+"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl+"&pay_orderid="+pay_orderid+"&key="+ key).toUpperCase();
    }

    /**
     * post请求接口
     *
     * @param url
     * @param parameters
     * @return
     */
    public static String executePost(String url,   Map<String, Object> parameters ) {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();// 创建一个可关闭的http客户端
        HttpPost post = new HttpPost(url);// 创建post请求
        String body = null;

        HttpEntity entity = new UrlEncodedFormEntity(createParam(parameters), Consts.UTF_8);

        if (!parameters.isEmpty() ) {
            try {
                // 建立一个NameValuePair数组，用于存储欲传送的参数
                /**
                 * addHeader，如果同名header已存在，则追加至原同名header后面。
                 * setHeader，如果同名header已存在，则覆盖一个同名header。
                 */
                //post.addHeader("Content-type", "application/json; charset=utf-8");// 设置请求头
                post.addHeader("Content-type", "application/x-www-form-urlencoded");// 设置请求头
               // post.setHeader("Accept", "application/json");
                //post.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));// 设置请求参数
                post.setEntity(entity);// 设置请求参数
                HttpResponse response = closeableHttpClient.execute(post);// 发送请求并返回一个HttpResponse
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    System.out.println("" + response.getStatusLine());
                }
                // 获取响应数据
                body = EntityUtils.toString(response.getEntity());
                // 关闭连接
                ((CloseableHttpResponse) response).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return body;
    }

    private static Iterable<? extends NameValuePair> createParam(Map<String, Object> map) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), (String) entry.getValue()));
        }
        return nameValuePairs;
    }


    /**
     * 验签
     * @param request
     * @return
     */
}
