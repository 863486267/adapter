package com.saas.adapter.tools;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * json工具类
 * 
 * @author Administrator
 *
 */
public final class JsonUtils {

	/**
	 * Object转泛型
	 * 
	 * @param obj
	 * @param cls
	 * @return
	 */
	public static final <T> T toObject(Object obj, Class<T> cls) {
		String json = toJson(obj);
		return toObject(json, cls);
	}

	/**
	 * jons转非泛型
	 * 
	 * @param json
	 * @param cls
	 * @return
	 */
	public static final <T> T toObject(String json, Class<T> cls) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		// 忽略多余字段
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			T t = mapper.readValue(json, cls);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * jons转泛型
	 * 
	 * @param json
	 * @param cls
	 * @return
	 */
	public static final <T> T toObject(String json, Class<T> parametrized, Class<?>... parameterClasses) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		JavaType javaType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
		try {
			T t = mapper.readValue(json, javaType);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 对象转json
	 * 
	 * @param obj
	 * @return
	 */
	public static final String toJson(Object obj) {
		if (obj == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(obj);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 对象克隆
	 * 
	 * @param src
	 * @param targetCls
	 * @return
	 */
	public static final <T> T clone(Object src, Class<T> targetCls) {
		String json = toJson(src);
		T t = toObject(json, targetCls);
		return t;
	}

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortMapByKey(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}

		Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {

				return str1.compareTo(str2);
			}
		});

		sortMap.putAll(map);

		return sortMap;
	}

}
