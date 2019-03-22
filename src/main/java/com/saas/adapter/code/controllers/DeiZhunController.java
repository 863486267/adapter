package com.saas.adapter.code.controllers;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeiZhunController {

    public static void main(String[] args) {

        String merchant_no="6201298";
        String merchant_key="8FA0E5A235E0191C2537";
        String order_no="586515614984";
        String pay_type="10012";
        String notify_url="http://www.abc.com";
        String return_url="http://www.abc.com";
        String trade_amount="10.00";
        String bank_code="1";
        String order_time=gettime();
        String s=merchant_no+merchant_key+order_no+trade_amount+pay_type+order_time;
        String sign=md5(s);
        System.out.println("s:"+s);
        System.out.println("sign:"+sign);

    }


    public static String md5(String s){
        MessageDigest messageDigest= null;
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            String md = new BigInteger(1, messageDigest.digest(s.getBytes("utf-8"))).toString(16);
            return md;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String gettime(){
        Date date=new Date();
        long times=date.getTime();
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time=format.format(times);
        return time;
    }

}
