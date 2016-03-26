package com.ec.api.service;

import com.ec.api.domain.PaymentInfo;
import com.ec.api.domain.query.PaymentInfoQuery;
import com.ec.api.service.result.Result;

public interface PaymentInfoService {
	
	/**
	 * 用户发起支付
	 * @param paymentInfo
	 * @return
	 */
	public Result userCreatePayment(PaymentInfo paymentInfo);
	
	//微信支付结果回调
	public String wxCallback(String callbackString);
}
