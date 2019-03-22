package com.saas.adapter.code.controllers;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


public class XinZhiFuH5 {

	private static final String nonce = UUID.randomUUID().toString().substring(0, 8);
	private static final String ORDER_URL = "http://ygjs.api.fz-xzf.com/channel/pay";
	private static final String QUERY_URL = "http://ygjs.api.fz-xzf.com/channel/search";
	private static final String key = "D9CCE36A9AA5433D85A59718E7D1F528";
	private static final RestTemplate restTemplate = new RestTemplate();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<String, String>();
        paramMap.add("tenantOrderNo", nonce);
        //paramMap.add("pageUrl", "https://www.hao123.com");
        paramMap.add("notifyUrl", "https://www.hao123.com");
        paramMap.add("amount", "1");
        paramMap.add("payType", "alipay");
        //paramMap.add("remark", "goods");
        System.out.println(createLinkString(paramMap) + "&key=" + key);
        String sign = null;
		try {
			sign = md5(createLinkString(paramMap) + "&key=" + key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(sign);
        paramMap.add("sign", sign);
        
        Map<String, Object> resultMap = restTemplate.postForObject(ORDER_URL, paramMap, Map.class);
        System.out.println("resultMap:"+resultMap);
        System.out.println(resultMap.get("status"));
        System.out.println(resultMap.get("url"));
	}
	
	public static final String createLinkString(Map<String, ?> paramValues) {
		List<String> keys = new ArrayList<String>(paramValues.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if(paramValues.get(key) == null || "".equals(paramValues.get(key))) {
            		continue;
            }
            String value = paramValues.get(key).toString();
            if(paramValues instanceof MultiValueMap) {
            		value = ((MultiValueMap<String, ?>) paramValues).getFirst(key).toString();
            }
            if("".equals(value.trim())) {
            	continue;
            }
			prestr = prestr + key + "=" + value + "&";
        }
        if(keys.size() > 0) {
        	prestr = prestr.substring(0, prestr.length() - 1);
        }
        return prestr;
	}
	
	public static final String md5(String str) throws Exception {
		StringBuilder sign = new StringBuilder();
		byte[] bytes = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}

}
