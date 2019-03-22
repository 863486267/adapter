package com.saas.adapter.interfaces;

import com.saas.adapter.po.PaidParams;
import com.saas.adapter.po.PaidQuery;
import com.saas.adapter.po.PayCallbackResult;
import com.saas.adapter.po.PayResult;

/**
 * 代付业务
 * 
 * @author Administrator
 *
 */
public interface PaidBusiness {
	/**
	 * 代付下单
	 * @param paidParams
	 * @return
	 */
	public PayResult paid(PaidParams paidParams);
	/**
	 * 代付回调 
	 * @param str
	 * @return
	 */
	public PayCallbackResult paidNotify(String str);
	/**
	 * 代付查询
	 * @param paidQuery
	 * @return
	 */
	public PayCallbackResult paidQuery(PaidQuery paidQuery);

}
