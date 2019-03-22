package com.saas.adapter.tools;

import org.springframework.stereotype.Component;

import com.saas.adapter.po.OrderReturn;


/**
 * 下单返回参数
 * 
 * @author Administrator
 *
 */
@Component
public class OrderReturnMain {
	/**
	 * 失败
	 * 
	 * @param message
	 * @return
	 */
	public OrderReturn failReturn(String message) {
		OrderReturn orderReturn = new OrderReturn();
		orderReturn.success = false;
		orderReturn.resultMessage = message;
		return orderReturn;
	}

	/**
	 * 成功
	 * 
	 * @param url
	 * @return
	 */
	public OrderReturn successReturn(String url) {
		OrderReturn orderReturn = new OrderReturn();
		orderReturn.success = true;
		orderReturn.resultMessage = "下单成功";
		orderReturn.url = url;
		return orderReturn;
	}
	
	/**
	 * 成功
	 * 
	 * @param url
	 * @return
	 */
	public OrderReturn successReturn(String url ,String msg) {
		OrderReturn orderReturn = new OrderReturn();
		orderReturn.success = true;
		orderReturn.resultMessage = msg;
		orderReturn.url = url;
		return orderReturn;
	}
	
	/**
	 * 成功
	 * 
	 * @param url
	 * @return
	 */
	public OrderReturn successReturn(String url ,String msg,String factMoney) {
		OrderReturn orderReturn = new OrderReturn();
		orderReturn.success = true;
		orderReturn.resultMessage = msg;
		orderReturn.url = url;
		orderReturn.factMoney = factMoney;
		return orderReturn;
	}
}
