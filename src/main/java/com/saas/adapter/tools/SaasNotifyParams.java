package com.saas.adapter.tools;

import org.springframework.stereotype.Component;

import com.saas.adapter.po.CallbackResult;


/**
 * 异步返回参数
 * 
 * @author Administrator
 *
 */
@Component
public class SaasNotifyParams {

	public CallbackResult successParams(String tradeNo ,String callbackResponse) {
		CallbackResult callbackResult = new CallbackResult();
		callbackResult.callbackResponse = callbackResponse;
		callbackResult.callbackResult = "支付成功";
		callbackResult.resultCode = "0";
		callbackResult.resultMessage = "成功";
		callbackResult.tradeNo = tradeNo;
		callbackResult.success = true;
		return callbackResult;
	}

	public CallbackResult failParams(String code,String callbackResponse) {
		CallbackResult callbackResult = new CallbackResult();
		callbackResult.callbackResponse = callbackResponse;
		callbackResult.callbackResult = "支付失败";
		callbackResult.resultCode = code;
		callbackResult.resultMessage = "支付失败";
		callbackResult.success = false;
		return callbackResult;
	}

	public CallbackResult errorParams(String message,String callbackResponse) {
		CallbackResult callbackResult = new CallbackResult();
		callbackResult.callbackResponse = callbackResponse;
		callbackResult.callbackResult = message;
		callbackResult.resultMessage = message;
		callbackResult.success = false;
		return callbackResult;
	}

}
