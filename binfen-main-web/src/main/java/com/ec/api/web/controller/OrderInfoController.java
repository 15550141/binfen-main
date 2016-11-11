package com.ec.api.web.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ec.api.common.utils.CookieUtils;
import com.ec.api.common.utils.JsonUtils;
import com.ec.api.common.utils.PaginatedArrayList;
import com.ec.api.domain.CartInfo;
import com.ec.api.domain.OrderDetail;
import com.ec.api.domain.OrderInfo;
import com.ec.api.domain.ReceiveAddr;
import com.ec.api.domain.SkuList;
import com.ec.api.domain.query.OrderInfoQuery;
import com.ec.api.service.CartService;
import com.ec.api.service.OrderInfoService;
import com.ec.api.service.ReceiveAddrService;
import com.ec.api.service.result.Result;
import com.ec.api.service.utils.EcUtils;
import com.ec.api.web.base.BaseController;

@Controller
@RequestMapping("/order")
public class OrderInfoController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(OrderInfoController.class);
	private OrderInfoService orderInfoService;
	
	private ReceiveAddrService receiveAddrService;
	private CartService cartService;
	
	private String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
	
	@RequestMapping(value="", method={RequestMethod.GET, RequestMethod.POST})
	public String index(HttpServletRequest request,HttpServletResponse response, ModelMap context){
		Integer uid = CookieUtils.getUid(request);
		CartInfo cartInfo = orderInfoService.index(uid, request);
		if(cartInfo == null){
			try {
				response.sendRedirect("http://www.binfenguoyuan.cn");
			} catch (IOException e) {
				log.error("", e);
			}
			return null;
		}
		//获得默认收货地址
		ReceiveAddr addr = receiveAddrService.getDefaultReceiveAddr(uid);
		//获取可配送的时间
		List<String> timeList = this.getHopeArrivalTimeList();
		context.put("timeList", timeList);
		context.put("addr", addr);
		context.put("cartInfo", cartInfo);
		return "order/order";
	}
	
	@RequestMapping(value="createOrder", method={RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody Result createOrder(Integer paymentType, Integer orderType, String address_id, String hopeArrivalTime, String remark, Integer sendType, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		Integer uid = CookieUtils.getUid(request);
		Result result = new Result();
		if(StringUtils.isBlank(address_id)){
			result.setSuccess(false);
			result.setResultMessage("地址信息不能为空");
			return result;
		}
		if(StringUtils.isBlank(hopeArrivalTime)){
			result.setSuccess(false);
			result.setResultMessage("请选择配送时间");
			return result;
		}
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setUserId(uid);
		orderInfo.setHopeArrivalTime(hopeArrivalTime);
		
		if(orderType == null || orderType == 0 || (orderType != 1 && orderType != 2 && orderType != 3)){
			result.setSuccess(false);
			result.setResultMessage("请选择支付方式");
			return result;
		}
		
		if(paymentType == null || paymentType == 0 || (paymentType != 1 && paymentType != 2 && paymentType != 3 && paymentType != 4)){
			result.setSuccess(false);
			result.setResultMessage("请选择支付方式");
			return result;
		}
		if(sendType == null){
			//默认是自有物流配送
			sendType = 2;
		}
		orderInfo.setSendType(sendType);
		orderInfo.setOrderType(orderType);
		orderInfo.setPaymentType(paymentType);
		orderInfo.setRemark(remark);
		return orderInfoService.createOrder(orderInfo, request, response);
	}
	
	@RequestMapping(value="my", method={RequestMethod.GET, RequestMethod.POST})
	public String my(Integer page, Integer type, Integer orderStatus, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		if(page == null){
			page = 1;
		}
		if(type == null){
			type = 1;
		}
		OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
		orderInfoQuery.setPageNo(page);
		orderInfoQuery.setUserId(CookieUtils.getUid(request));
		
		PaginatedArrayList<OrderInfo> orders = null;
		
		
		if(type == 2){
			//已完成订单
			orderInfoQuery.setOrderStatus(50);
			orders = this.orderInfoService.getOrderInfosByOrderInfoQuery(orderInfoQuery);
		}else if(type == 3){
			//已取消订单
			orderInfoQuery.setOrderStatus(51);
			orders = this.orderInfoService.getOrderInfosByOrderInfoQuery(orderInfoQuery);
		}else{
			type = 1;
			orders = this.orderInfoService.getUnfinishedOrder(orderInfoQuery);
		}
		
		context.put("orders", orders);
		context.put("type", type);
		context.put("page", page);
		
		return "/order/my";
	}
	
	@RequestMapping(value="more", method={RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody Result more(Integer page, Integer type, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		if(page == null){
			page = 1;
		}
		if(type == null){
			type = 1;
		}
		OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
		orderInfoQuery.setPageNo(page);
		orderInfoQuery.setUserId(CookieUtils.getUid(request));
		
		PaginatedArrayList<OrderInfo> orders = null;
		
		
		if(type == 2){
			//已完成订单
			orderInfoQuery.setOrderStatus(50);
			orders = this.orderInfoService.getOrderInfosByOrderInfoQuery(orderInfoQuery);
		}else if(type == 3){
			//已取消订单
			orderInfoQuery.setOrderStatus(51);
			orders = this.orderInfoService.getOrderInfosByOrderInfoQuery(orderInfoQuery);
		}else{
			type = 1;
			orders = this.orderInfoService.getUnfinishedOrder(orderInfoQuery);
		}
		
		Result result = new Result();
		
		if(orders == null || orders.size() == 0){
			result.setSuccess(false);
			return result;
		}
		
		result.setSuccess(true);
		result.setResult(orders);
		
		return result;
	}
	
	@RequestMapping(value="detail", method={RequestMethod.GET, RequestMethod.POST})
	public String detail(Integer orderId, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		if(orderId == null){
			return "error";
		}
		
		Integer userId = CookieUtils.getUid(request);
		OrderInfo order = this.orderInfoService.getOrderInfoByOrderIdAndUserId(orderId, userId);
		if(order == null){
			return "error";
		}
		context.put("order", order);
		
		return "/order/detail";
	}
	
	@RequestMapping(value="orderCancle", method={RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody Result orderCancle(Integer orderId, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		Integer uid = CookieUtils.getUid(request);
		return orderInfoService.orderCancle(orderId, uid, request, response);
	}
	
	@RequestMapping(value="testWxPay", method={RequestMethod.GET, RequestMethod.POST})
	public String testWxPay(HttpServletRequest request,HttpServletResponse response, ModelMap context){
		return "/order/testWxPay";
	}
	
	@RequestMapping(value="chosePayment", method={RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody Result chosePayment(Integer paymentType, Integer orderType, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		Result result = new Result();
		Map<String, Object> map = new HashMap<String, Object>();
		result.setSuccess(true);
		map.put("pmt_goods", 0);
		
		//之前逻辑是，如果是在线支付，订单满19元减5元逻辑。现在删除掉了。
//		Integer uid = CookieUtils.getUid(request);
//		Integer count = orderInfoService.getEffectiveOrderCount(uid);
//		if(count == null || count == 0){ //没下过订单
//			if(orderType == 1){ //在线支付
//				CartInfo cartInfo = cartService.getCartInfoByCookie(uid, request);
//				map.put("pmt_goods", cartInfo.getTotlePreferentialPrice());
//			}
//		}
		result.setResult(map);
		return result;
	}

	private List<String> getHopeArrivalTimeList(){
		//定义配送时间
		//1、10-12	2、12-14		3、14-17		4、17-20
		List<String> list = new ArrayList<String>();
		
		Calendar cal = Calendar.getInstance();
		Integer nowHour = cal.get(Calendar.HOUR_OF_DAY);
		String nowDate = (cal.get(Calendar.MONTH)+ 1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"|"+weekDays[(cal.get(Calendar.DAY_OF_WEEK) - 1)];
		if(nowHour < 10){
			list.add(nowDate + " 10:00-12:00");
			list.add(nowDate + " 12:00-14:00");
			list.add(nowDate + " 14:00-18:00");
//			list.add(nowDate + " 17:00-20:00");
		}else if(nowHour < 12){
			list.add(nowDate + " 12:00-14:00");
			list.add(nowDate + " 14:00-18:00");
//			list.add(nowDate + " 17:00-20:00");
		}else if(nowHour < 15){
			list.add(nowDate + " 14:00-18:00");
//			list.add(nowDate + " 17:00-20:00");
//		}else if(nowHour < 19){
//			list.add(nowDate + " 17:00-20:00");
		}
		
		cal.add(Calendar.DATE, 1);
		System.out.println();
		String nextDate = (cal.get(Calendar.MONTH)+ 1)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"|"+weekDays[(cal.get(Calendar.DAY_OF_WEEK) - 1)];
		list.add(nextDate + " 10:00-12:00");
		list.add(nextDate + " 12:00-14:00");
		list.add(nextDate + " 14:00-18:00");
//		list.add(nextDate + " 17:00-20:00");
		return list;
	}

	public void setOrderInfoService(OrderInfoService orderInfoService) {
		this.orderInfoService = orderInfoService;
	}

	public void setReceiveAddrService(ReceiveAddrService receiveAddrService) {
		this.receiveAddrService = receiveAddrService;
	}

	public void setCartService(CartService cartService) {
		this.cartService = cartService;
	}


}
