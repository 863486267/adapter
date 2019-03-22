package com.saas.adapter.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import groovyjarjarcommonscli.ParseException;

public class DateUtil {


	/*
	 * 获取下一个月第一天凌晨1点
	 */
	public static Date nextMonthFirstDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 1); // 设置为每月凌晨1点
		calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置为每月1号
		calendar.add(Calendar.MONTH, 1); // 月份加一，得到下个月的一号
//        calendar.add(Calendar.DATE, -1);     下一个月减一为本月最后一天
		return calendar.getTime();
	}

	/**
	 * 获取第二天凌晨0点毫秒数
	 * 
	 * @return
	 */
	public static Date nextDayFirstDate() throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 00);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	// *********

	/**
	 * 获取当前时间到下个月凌晨1点相差秒数
	 * 
	 * @return
	 */
	public static Long getSecsToEndOfCurrentMonth() {

		Long secsOfNextMonth = nextMonthFirstDate().getTime();
		// 将当前时间转为毫秒数
		Long secsOfCurrentTime = new Date().getTime();
		// 将时间转为秒数
		Long distance = (secsOfNextMonth - secsOfCurrentTime) / 1000;
		if (distance > 0 && distance != null) {
			return distance;
		}

		return new Long(0);

	}

	/**
	 * 获取当前时间到明天凌晨0点相差秒数
	 * 
	 * @return
	 */
	public static Long getSecsToEndOfCurrentDay() throws ParseException {

		Long secsOfNextDay = nextDayFirstDate().getTime();
		// 将当前时间转为毫秒数
		Long secsOfCurrentTime = new Date().getTime();
		// 将时间转为秒数
		Long distance = (secsOfNextDay - secsOfCurrentTime) / 1000;
		if (distance > 0 && distance != null) {
			return distance;
		}

		return new Long(0);

	}

	/**
	 * 根据格式 生成时间
	 * @param format
	 * @return
	 */
	public static String generateTime(String format){
		DateTimeFormatter pattern = DateTimeFormatter.ofPattern(format);
		return LocalDateTime.now().format(pattern);
	}
}
