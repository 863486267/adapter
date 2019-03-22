package com.saas.adapter.po;

import org.springframework.stereotype.Component;

@Component
public class PaidNotify {

	public PayCallbackResult success(String tradeNo, String callbackResult, String callbackResponse) {
		PayCallbackResult payCallbackResult = new PayCallbackResult();
		payCallbackResult.success = true;
		payCallbackResult.callbackResponse = callbackResponse;
		payCallbackResult.callbackResult = callbackResult;
		payCallbackResult.tradeNo = tradeNo;
		return payCallbackResult;
	}

	public PayCallbackResult fail(String tradeNo, String callbackResult, String callbackResponse) {
		PayCallbackResult payCallbackResult = new PayCallbackResult();
		payCallbackResult.success = false;
		payCallbackResult.callbackResponse = callbackResponse;
		payCallbackResult.callbackResult = callbackResult;
		payCallbackResult.tradeNo = tradeNo;
		return payCallbackResult;
	}

}
