package com.saas.adapter.interfaces;

import com.saas.adapter.po.CallbackResult;
import com.saas.adapter.po.Order;
import com.saas.adapter.po.OrderReturn;
import com.saas.adapter.po.Parameter;

/**
 * 支付业务接口
 * @author Administrator
 *
 */
public interface PayBusiness {
	/**
	 * 下单
	 * @param parameter
	 * @return
	 */
	public OrderReturn pay(Parameter parameter);
	/**
	 * 回调
	 */
	public CallbackResult notify(String str);
	/**
	 * 订单查询
	 * @param order
	 * @return
	 */
	public CallbackResult query(Order order);
	
}
