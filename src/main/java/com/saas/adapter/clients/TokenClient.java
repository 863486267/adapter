package com.saas.adapter.clients;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.saas.adapter.tools.DateUti;

import groovyjarjarcommonscli.ParseException;

/**
 * 获取支付凭证
 * 
 * @author Administrator
 *
 */
@Component
public class TokenClient {

	private static String KEY_PAY_PARAMS = "key:hehe.appid.";

	private static String KEY_PAY_LIMIT = "key:limit.appid.";

	private static String KEY_PAY_QUALIFICATION = "key:QUALIFICATION.appid.";

	private static String KEY_NUMBER_LIMIT = "key:NUMBER.appid.";

	private static String KEY_MONERY_RANDOMKNOCK = "key:RANDOMKNOCK.appid.";

	private static Logger LOG = LoggerFactory.getLogger(TokenClient.class);

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * 资质未支付交易累计条数
	 */
	public void addNumber(String id, Integer count) {
		LOG.info("资质未支付交易累计条数");
		String key = KEY_NUMBER_LIMIT + id;
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		synchronized (key) {
			String number = opsForValue.get(key);
			LOG.info("资质未支付交易累计条数,当前ID=" + id + ",累计条数=" + number);
			if (StringUtils.isBlank(number)) {
				LOG.info("资质未支付交易累计条数为空初始值赋予1,");
				number = "1";
				opsForValue.set(key, number, 1, TimeUnit.DAYS);
				return;
			}
			if (Long.valueOf(count) - Long.valueOf(number) == 0 || Long.valueOf(count) - Long.valueOf(number) < 0) {
				// 关闭资质
				try {
					LOG.info("资质关闭(id=" + id + ")");
//					String url = "http://172.19.233.6:15860/aptitude/setAptitudeFalse/" + id;//联梦
//					String url = "http://172.18.93.108:15860/aptitude/setAptitudeFalse/" + id;//闪付
					String url = "http://120.78.178.51:15860/aptitude/setAptitudeFalse/" + id;// 新商宝weipay
//					String url = "http://172.18.204.57:15860/aptitude/setAptitudeFalse/" + id;// BB
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
					HttpEntity<String> entity = new HttpEntity<String>(headers);
					this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
					LOG.info("移除资质累计(id=" + id + ")");
					stringRedisTemplate.delete(key);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String addNumber = Long.toString(Long.valueOf(number) + Long.valueOf("1"));
			opsForValue.set(key, addNumber, 1, TimeUnit.DAYS);
			return;
		}
	}

	/**
	 * 资质未支付交易累计条数(取消)
	 */
	public void removeByNumber(String id) {
		String key = KEY_NUMBER_LIMIT + id;
		stringRedisTemplate.delete(key);
	}

	/**
	 * 获取资质实时交易金额
	 * 
	 * @param name
	 * @return
	 */
	public Long getPayMongIng(String name) {
		LOG.info("获取资质实时交易金额");
		String key = KEY_PAY_LIMIT + name;
		String money = null;
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		money = opsForValue.get(key);
		LOG.info("获取资质实时交易金额:" + money);
		if (StringUtils.isBlank(money)) {
			synchronized (key) {
				money = opsForValue.get(key);
				if (StringUtils.isNotBlank(money)) {
					return Long.valueOf(money);
				}
				try {
					LOG.info("资质实时交易金额初始化");
					opsForValue.set(key, "0", DateUti.getSecsToEndOfCurrentDay(), TimeUnit.SECONDS);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				money = "0";
				return Long.valueOf(money);
			}
		}
		return Long.valueOf(money);
	}

	/**
	 * 资质交易金额累加
	 * 
	 * @param name
	 * @param nowMoney
	 */
	public void setMoney(String name, String nowMoney) {
		LOG.info("资质交易金额累加(name=" + name + ",nowMoney=" + nowMoney + ")");
		String key = KEY_PAY_LIMIT + name;
		String money = null;
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		synchronized (key) {
			money = opsForValue.get(key);
			try {
				if (StringUtils.isBlank(money)) {
					opsForValue.set(key, nowMoney, DateUti.getSecsToEndOfCurrentDay(), TimeUnit.SECONDS);
					return;
				}
				money = Long.toString((Long.valueOf(money) + Long.valueOf(nowMoney)));
				opsForValue.set(key, money, DateUti.getSecsToEndOfCurrentDay(), TimeUnit.SECONDS);
				return;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取随机立减金额
	 * @param AptitudeId
	 * @return
	 */
	public String getMoneryRandomKnock(String AptitudeId) {
		String key = KEY_MONERY_RANDOMKNOCK + AptitudeId;
		String money = null;
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		synchronized (key) {
			money = opsForValue.get(key);
			if (StringUtils.isBlank(money) || Long.valueOf(money) == 50) {
				opsForValue.set(key, "0");
				return "0";
			}
			money = Long.toString((Long.valueOf(money) + Long.valueOf("1")));
			opsForValue.set(key, money);
			return money;
		}
	}

	/**
	 * 移除订单资质名称
	 * 
	 * @param no
	 */
	public void removeQualificationByOrder(String no) {
		LOG.info("移除订单资质名称");
		String key = KEY_PAY_QUALIFICATION + no;
		stringRedisTemplate.delete(key);
	}

	/**
	 * 保存订单交易所属资质名称
	 * 
	 * @param no                交易备注
	 * @param qualificationName
	 * @return
	 */
	public boolean savePayQualification(String no, String qualificationName) {
		LOG.info("保存订单交易所属资质名称");
		try {
			String key = KEY_PAY_QUALIFICATION + no;
			ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
			if (StringUtils.isNotBlank(qualificationName)) {
				opsForValue.set(key, qualificationName, 1, TimeUnit.DAYS);
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * 根据ID获取订单资质名称
	 * 
	 * @param id
	 * @return
	 */
	public String getQualification(String id) {
		String qualification = null;
		try {
			String key = KEY_PAY_QUALIFICATION + id;
			ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
			qualification = opsForValue.get(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return qualification;
	}

	/**
	 * 保存支付回调地址
	 * 
	 * @param outTradeNo
	 * @param notifyUrl
	 * @return
	 */
	public boolean savePayParams(String no, String notifyUrl) {
		boolean retCode = false;
		try {
			String key = KEY_PAY_PARAMS + no;
			ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
			if (StringUtils.isNotBlank(notifyUrl)) {
				opsForValue.set(key, notifyUrl, 20, TimeUnit.MINUTES);
				retCode = true;
				return retCode;
			}
		} catch (Exception e) {
			e.printStackTrace();
			retCode = false;
			return retCode;
		}
		return retCode;
	}


	
	/**
	 * 保存支付链接地址
	 * 
	 * @param outTradeNo
	 * @param notifyUrl
	 * @return
	 */
	public boolean savePayUrl(String no, String notifyUrl ,int time) {
		boolean retCode = false;
		try {
			String key = KEY_PAY_PARAMS + no;
			ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
			if (StringUtils.isNotBlank(notifyUrl)) {
				opsForValue.set(key, notifyUrl, time, TimeUnit.MINUTES);
				retCode = true;
				return retCode;
			}
		} catch (Exception e) {
			e.printStackTrace();
			retCode = false;
			return retCode;
		}
		return retCode;
	}
	

	/**
	 * 获取支付参数
	 * 
	 * @param no
	 * @return
	 */
	public String getPayParams(String no) {
		String notifyUrl = null;
		try {
			String key = KEY_PAY_PARAMS + no;
			ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
			notifyUrl = opsForValue.get(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return notifyUrl;
	}

	/**
	 * 移除支付信息
	 * 
	 * @param no
	 */
	public void remove(String no) {
		LOG.info("移除支付信息(no=" + no + ")");
		String key = KEY_PAY_PARAMS + no;
		stringRedisTemplate.delete(key);
	}

}
