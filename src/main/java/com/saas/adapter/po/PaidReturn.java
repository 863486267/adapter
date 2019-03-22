package com.saas.adapter.po;

import org.springframework.stereotype.Component;
/**
 * 代付同步返回SAAS
 * @author Administrator
 *
 */
@Component
public class PaidReturn {

	public PayResult success(String tradeNo){
		PayResult payResult = new PayResult();
		payResult.success = true;
		payResult.tradeNo = tradeNo;
		return payResult;
	}
	
	public PayResult fail(String resultCode,String resultMessage){
		PayResult payResult = new PayResult();
		payResult.success = false;
		payResult.resultCode = resultCode;
		payResult.resultMessage = resultMessage;
		return payResult;
	}
	
}
