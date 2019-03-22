package com.saas.adapter.tools;

import java.math.BigInteger;
import java.security.MessageDigest;

public class tool {

    //MD5加密 小写
    public static String getMD5(String str){
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8位字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            //一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方）
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(getMD5("p_Amount=20000&p_ChildChannelNo=Milan&p_MainChannelNo=Italy&p_MerchantNo=20190319130009171374&p_NotifyUrl=http://www.baidu.com&p_OrderNo=5413511321&p_ProductName=aaaa&p_ReturnUrl=http://www.baidu.com&AppId=ax2WpOg6e7qGoUCs1Oy9QITq8dT5dasJwrdrkAnE&AppKey=OHQheZD6DnPeUBv62VpfLPcOhCadXIg3Wqp5jMbP"));
        System.out.println(MD5.md5("p_ChannelNo=测试通道&p_MerchantNo=测试商户号&p_OrderNo=测试订单编号&AppId=BkDOcxUveVoXdzwN2tyg&AppKey=OaUbW8SCt7OGwEJ80F4m"));
    }

    //加签




    public static String string = "0123456789";

    public static String getRandomString(int length){
        StringBuffer sb = new StringBuffer();
        int len = string.length();
        for (int i = 0; i < length; i++) {
            sb.append(string.charAt(getRandom(len-1)));
        }
        return sb.toString();
    }

    public static int getRandom(int count){
        return (int) Math.round(Math.random() * (count));
    }
}
