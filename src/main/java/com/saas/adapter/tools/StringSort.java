package com.saas.adapter.tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字符处理工具类
 * 
 * @author Administrator
 *
 */
public class StringSort {

	/**
	 * 分转元
	 *
	 * @param amount
	 * @return
	 * @throws Exception
	 */
	public String changeF2Y(String amount) throws Exception {
		if (!amount.matches("\\-?[0-9]+")) {
			throw new Exception("分转元");
		}
		return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(100)).toString();
	}

	/**
	 * 将元为单位的参数转换为分 , 只对小数点前2位支持
	 *
	 * @param yuan
	 * @return
	 * @throws Exception
	 */
	public String yuan2FenInt(String yuan) {
		BigDecimal fenBd = new BigDecimal(yuan).multiply(new BigDecimal(100));
		fenBd = fenBd.setScale(0, BigDecimal.ROUND_HALF_UP);
		return fenBd.toString();
	}

	/**
	 * 将map 转为 string
	 * true 字典排序
	 * @param map
	 * @return
	 */
	public String getUrlParamsByMap(Map<String, String> map, boolean isSort) {
		if (map == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<String>(map.keySet());
		if (isSort) {
			Collections.sort(keys);
		}
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = String.valueOf(map.get(key));
			sb.append(key + "=" + value);
			sb.append("&");
		}
		String s = sb.toString();
		if (s.endsWith("&")) {
			s = s.substring(0, s.lastIndexOf("&"));
		}
		/*
		 * for (Map.Entry<String, Object> entry : map.entrySet()) {
		 * sb.append(entry.getKey() + "=" + entry.getValue()); sb.append("&"); }
		 * String s = sb.toString(); if (s.endsWith("&")) { //s =
		 * StringUtils.substringBeforeLast(s, "&"); s = s.substring(0,
		 * s.lastIndexOf("&")); }
		 */
		return s;
	}
	
	  /**
	   * 将 String 转为 map
	   * 
	   * @param param
	   *            aa=11&bb=22&cc=33
	   * @return
	   */
	  public static Map<String, Object> getUrlParams(String param) {
	      Map<String, Object> map = new HashMap<String, Object>();
	      if ("".equals(param) || null == param) {
	          return map;
	      }
	      String[] params = param.split("&");
	      for (int i = 0; i < params.length; i++) {
	          String[] p = params[i].split("=");
	          if (p.length == 2) {
	              map.put(p[0], p[1]);
	          }
	      }
	      return map;
	  }
	
}
