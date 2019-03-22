package com.saas.adapter.code.controllers.chaofan;

import java.net.URLEncoder;
import java.util.*;

public class SignUtils
{

    public static Map<String, String> parseQuery(String strQuery)
    {
        final Map<String, String> map = new HashMap();
        final String[] pairs = strQuery.split("&");
        for (String pair : pairs) {
            final int index = pair.indexOf("=");
            final String key = index > 0 ? pair.substring(0, index) : pair;
            final String value = index > 0 && pair.length() > index + 1 ? pair.substring(index + 1) : null;
            map.put(key, value);
        }
        return map;
    }

    /**
     验证返回参数

     @param params
     @param key
     @return
     */
    public static boolean checkParam(Map<String, String> params, String key)
    {
        boolean result = false;
        if (params.containsKey("sign")) {
            String sign = params.get("sign");
            params.remove("sign");
            params.put("key", key);
            StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
            SignUtils.buildPayParams(buf, params, false);
            String preStr = buf.toString();
            String signRecieve = MD5.sign(preStr, "utf-8");
            result = sign.equalsIgnoreCase(signRecieve);
        }
        return result;
    }

    /**
     过滤参数

     @param sArray
     @return
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray)
    {
        if (sArray == null || sArray.size() <= 0) {
            return new HashMap();
        }
        Map<String, String> result = new HashMap(sArray.size());
        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     将map转成String

     @param payParams
     @return
     */
    public static String payParamsToString(Map<String, String> payParams)
    {
        return payParamsToString(payParams, false);
    }

    public static String payParamsToString(Map<String, String> payParams, boolean encoding)
    {
        return payParamsToString(new StringBuilder(), payParams, encoding);
    }

    public static String payParamsToString(StringBuilder sb, Map<String, String> payParams, boolean encoding)
    {
        buildPayParams(sb, payParams, encoding);
        return sb.toString();
    }

    public static void buildPayParams(StringBuilder sb, Map<String, String> payParams, boolean encoding)
    {
        List<String> keys = new ArrayList(payParams.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(key).append("=");
            if (encoding) {
                sb.append(urlEncode(payParams.get(key)));
            }
            else {
                sb.append(payParams.get(key));
            }
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
    }

    public static String urlEncode(String str)
    {
        try {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch (Throwable e) {
            return str;
        }
    }

}
