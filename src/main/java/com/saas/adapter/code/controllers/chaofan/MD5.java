package com.saas.adapter.code.controllers.chaofan;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

/**
 功能：MD5签名
 */
public class MD5
{

    /**
     签名字符串

     @param text 需要签名的字符串
     @param input_charset 编码格式
     @return 签名结果
     */
    public static String sign(String text, String input_charset)
    {
        System.out.println(text);
        return DigestUtils.md5Hex(getContentBytes(text, input_charset)).toUpperCase();
    }

    /**
     签名字符串

     @param text 需要签名的字符串
     @param sign 签名结果
     @param input_charset 编码格式
     @return 签名结果
     */
    public static boolean verify(String text, String sign, String input_charset)
    {
        String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
        if (mysign.equals(sign)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     @param content
     @param charset
     @return
     @throws SignatureException
     @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset)
    {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

}
