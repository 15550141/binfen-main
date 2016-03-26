package com.ec.api.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ec.api.common.utils.PaginatedArrayList;
import com.ec.api.domain.CartInfo;
import com.ec.api.domain.OrderInfo;
import com.ec.api.domain.OrderDetail;
import com.ec.api.domain.query.OrderInfoQuery;
import com.ec.api.service.result.Result;

public interface OrderInfoService {
	
	/**
	 * 下单页面
	 * @param uid
	 * @param request
	 * @return
	 */
	public CartInfo index(Integer uid, HttpServletRequest request);
	
	/**
	 * 创建订单
	 * @return
	 */
	public Result createOrder(final OrderInfo order, HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 获得未完成订单
	 * @param orderInfoQuery
	 * @return
	 */
	public PaginatedArrayList<OrderInfo> getUnfinishedOrder(OrderInfoQuery orderInfoQuery);
	
	/**
	 * 用户所属订单列表查看（细分）
	 * @return
	 */
	public PaginatedArrayList<OrderInfo> getOrderInfosByOrderInfoQuery(OrderInfoQuery orderInfoQuery);
	
	/**
	 * 根据订单号和用户id获取订单详细信息
	 * @param orderId
	 * @return
	 */
	public OrderInfo getOrderInfoByOrderIdAndUserId(Integer orderId, Integer userId);
	
	/**
	 *  取消订单
	 * @return
	 */
	public Result orderCancle(Integer orderId, Integer uid, HttpServletRequest request,
			HttpServletResponse response);
	
	/**
	 * 查看该用户是否未生成过订单
	 * @param uid
	 * @return
	 */
	public Integer getEffectiveOrderCount(Integer uid);
	
}
