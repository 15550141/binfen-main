package com.ec.api.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ec.api.common.utils.*;
import com.ec.api.dao.*;
import com.ec.api.domain.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.ec.api.domain.query.OrderInfoQuery;
import com.ec.api.domain.query.PromotionSkuQuery;
import com.ec.api.service.CartService;
import com.ec.api.service.OrderInfoService;
import com.ec.api.service.PaymentInfoService;
import com.ec.api.service.ReceiveAddrService;
import com.ec.api.service.UmpInfoService;
import com.ec.api.service.result.Result;
import com.ec.api.service.utils.CartUtils;
import com.ec.api.service.utils.EcUtils;
import com.ec.api.service.vo.UmpPayServiceResultVo;

@Service(value="orderInfoService")
public class OrderInfoServiceImpl implements OrderInfoService {
	private static final Logger log = LoggerFactory.getLogger(OrderInfoServiceImpl.class);

	private UserInfoDao userInfoDao;
	private OrderInfoDao orderInfoDao;
	private ItemDao itemDao;
	private OrderDetailDao  orderDetailDao;
	private SkuDao skuDao;
	private PromotionSkuDao promotionSkuDao;
	private PromotionInfoDao promotionInfoDao;
    private ConsigneeInfoDao consigneeInfoDao;
	private DataSourceTransactionManager transactionManager;
	private UmpInfoDao umpInfoDao;
	private UmpInfoService umpInfoService;
	private CartService cartService;
	private ReceiveAddrService receiveAddrService;
	private PaymentInfoService paymentInfoService;
	private TaskDao taskDao;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	@Override
	public CartInfo index(Integer uid, HttpServletRequest request) {
		return  cartService.getCartInfoByCookie(uid, request);
	}
	
	@Override
	public Result createOrder(final OrderInfo order, HttpServletRequest request, HttpServletResponse response) {
		final Result result = new Result();
		try{
			UserInfo userInfo = this.userInfoDao.selectByUserId(order.getUserId());

			CartInfo cartInfo = cartService.getCartInfoByCookie(order.getUserId(), request);
			
			if(cartInfo == null){
				result.setSuccess(false);
				result.setResultMessage("商品信息异常");
				return result;
			}
			
			//设置基本信息
			this.setBasicOrderInfo(order);
			order.setIp(HttpUtils.getRemoteIp(request));

			//如果是分销商，则打上分销商标记
			if(FlagBitUtil.checkSign(userInfo.getProperties(), 1)){
				order.setProperties(FlagBitUtil.sign(order.getProperties(), 1));
			}
			//如果是自提订单
			if(order.getSendType() == 1) {
				//设置订单总金额
				order.setOrderMoney(((cartInfo.getTotleSalePrice().subtract(cartInfo.getFreightMoney())).multiply(new BigDecimal(100))).intValue());
			}else{
				order.setFreightMoney(cartInfo.getFreightMoney().multiply(new BigDecimal(100)).intValue());
				//设置订单总金额
				order.setOrderMoney((cartInfo.getTotleSalePrice().multiply(new BigDecimal(100))).intValue());
			}

			//设置订单总优惠金额
			order.setDiscountMoney((cartInfo.getTotlePreferentialPrice().multiply(new BigDecimal(100))).intValue());
			//设置订单优惠明细
//			if(cartInfo.getTotlePreferentialPrice().compareTo(new BigDecimal(0)) > 0){
//				order.setDiscountInfo("首单满19元减5元");
//			}
			
			//如果是货到付款订单
			if(order.getOrderType() == 1){
				order.setOrderStatus(1);
			}else{
				//货到付款和月结订单，订单状态直接变为待发货
				order.setOrderStatus(8);
			}
			//商品信息
			final List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
			
			List<CartSku> cartSkus = cartInfo.getSkuList();
			for(int i = 0 ; i < cartSkus.size() ; i++){
				OrderDetail orderDetail = new OrderDetail();
				CartSku sku = cartSkus.get(i);

				if(sku.getItemStatus() != 1){
//					throw new RuntimeException("商品["+sku.getName()+"]已下架");
				}
				//如果是分销商品
				if(FlagBitUtil.checkSign(sku.getProperties(), PropertyConstants.USER_FENXIAOSHANG)){
					orderDetail.setProperties(FlagBitUtil.sign(orderDetail.getProperties(), PropertyConstants.USER_FENXIAOSHANG));
				}
				orderDetail.setUid(order.getUserId());
				orderDetail.setItemId(sku.getItemId());
				orderDetail.setItemImage(sku.getImage());
				orderDetail.setItemName(sku.getName());
				orderDetail.setNum(sku.getNum());
				orderDetail.setPrice((sku.getSkuPrice().multiply(new BigDecimal(100))).intValue());
				orderDetail.setSalesProperty(sku.getSalesProperty());
				orderDetail.setSalesPropertyName(sku.getSalesPropertyName());
				orderDetail.setSkuId(sku.getSkuId());
				orderDetailList.add(orderDetail);
			}
			
			new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				Integer orderId = orderInfoDao.insert(order);
				order.setOrderId(orderId);

				for(int i=0;i<orderDetailList.size();i++){
					OrderDetail orderDetail = orderDetailList.get(i);
					orderDetail.setOrderId(orderId);
					orderDetailDao.insert(orderDetail);

					//扣库存
					Item item = new Item();
					item.setItemId(orderDetail.getItemId());
					item.setStockNum(new BigDecimal(orderDetail.getNum()));

					Integer delState = itemDao.delStock(item);

					if(delState == null || delState == 0){
						//TODO 抛个异常，回滚事务
						throw new RuntimeException("商品["+orderDetail.getItemName()+"]库存不足");
					}

					//TODO 扣除促销库存
//						for(int j=0;j<delPromotionInfoStock.size();j++){
//							int delResult = promotionInfoDao.updatePromotionInfoStock(delPromotionInfoStock.get(j));
//							if(delResult <= 0){
//								throw new RuntimeException("促销商品库存不足,扣减失败");
//							}
//						}

				}

				//添加任务表
				Task task = new Task();
				Map<String, Integer> map = new HashMap<String, Integer>();
				map.put("orderId", order.getOrderId());
				map.put("userId", order.getUserId());
				task.setContent(JsonUtils.writeValue(map));//内容
				task.setStatus(0);//初始状态
				task.setType(1);//下单成功任务
				task.setYn(1);//有效
				taskDao.insert(task);
				EcUtils.setSuccessResult(result);
				}
			});
			CartUtils.clearCookies(response);//清除
			
			if(order.getPaymentType() == 3){//微信支付
				PaymentInfo paymentInfo = new PaymentInfo();
				paymentInfo.setOrderId(order.getOrderId());
				paymentInfo.setOrderPayType(1);
				paymentInfo.setUid(order.getUserId());
				return paymentInfoService.userCreatePayment(paymentInfo);
			}
			
		}catch (RuntimeException e) {
			log.error("下单异常，userId:"+order.getUserId(), e);
			result.setResultMessage(e.getMessage());
			result.setSuccess(false);
			result.setResult(false);
		}catch (Exception e) {
			log.error("下单异常，userId:"+order.getUserId(), e);
			EcUtils.setExceptionResult(result);
		}
		
		return result;
	}
	
	@Override
	public PaginatedArrayList<OrderInfo> getUnfinishedOrder(OrderInfoQuery orderInfoQuery) {
		//分页反参
		PaginatedArrayList<OrderInfo> orders = new PaginatedArrayList<OrderInfo>(orderInfoQuery.getPageNo(),orderInfoQuery.getPageSize());
		try{
			//获取总数量
			int count = orderInfoDao.countByUnfinishedOrder(orderInfoQuery);
			if(count > 0){
				//设置反参总数量
				orders.setTotalItem(count);
				//设置起始行
				orderInfoQuery.setStart(orders.getStartRow());
				//查询用户订单信息
				List<OrderInfo> orderInfoList= orderInfoDao.selectByUnfinishedOrder(orderInfoQuery);
				//加载商品信息
				for(OrderInfo orderInfo : orderInfoList){
					List<OrderDetail> orderDetailList = orderDetailDao.selectByOrderId(orderInfo.getOrderId());
					orderInfo.setOrderDetails(orderDetailList);
				}
				
				orders.addAll(orderInfoList);
			}
		}catch (Exception e) {
			log.error("", e);
		}
		
		return orders;
		
	}
	
	/**
	 * 用户所属订单列表查看（细分）
	 * @return
	 */
	@Override
	public PaginatedArrayList<OrderInfo> getOrderInfosByOrderInfoQuery(OrderInfoQuery orderInfoQuery) {
		//分页反参
		PaginatedArrayList<OrderInfo> orders = new PaginatedArrayList<OrderInfo>(orderInfoQuery.getPageNo(),orderInfoQuery.getPageSize());
		try{
			//获取总数量
			int count = orderInfoDao.countByCondition(orderInfoQuery);
			if(count > 0){
				//设置反参总数量
				orders.setTotalItem(count);
				//设置起始行
				orderInfoQuery.setStart(orders.getStartRow());
				//查询用户订单信息
				List<OrderInfo> orderInfoList= orderInfoDao.selectByConditionForPage(orderInfoQuery);
				//加载商品信息
				for(OrderInfo orderInfo : orderInfoList){
					List<OrderDetail> orderDetailList = orderDetailDao.selectByOrderId(orderInfo.getOrderId());
					orderInfo.setOrderDetails(orderDetailList);
				}
				
				orders.addAll(orderInfoList);
			}
		}catch (Exception e) {
			log.error("", e);
		}
		
		return orders;
	}
	
	@Override
	public OrderInfo getOrderInfoByOrderIdAndUserId(Integer orderId, Integer userId) {
		OrderInfo order = null;
		try{
			OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
			orderInfoQuery.setUserId(userId);
			orderInfoQuery.setOrderId(orderId);
			
			List<OrderInfo> list = this.orderInfoDao.selectByCondition(orderInfoQuery);
			
			if(list == null || list.size() == 0){
				return null;
			}
			
			order = list.get(0);
			order.setOrderDetails(orderDetailDao.selectByOrderId(order.getOrderId()));
			
		}catch (Exception e) {
			log.error("", e);
		}
		return order;
	}
	
	@Override
	public Result orderCancle(Integer orderId, Integer uid, HttpServletRequest request,
			HttpServletResponse response) {
		
		Result result = new Result();
		try{
			OrderInfo orderInfo = this.orderInfoDao.selectByOrderId(orderId);
			if(orderInfo == null){
				result.setSuccess(false);
				result.setResultCode("9004");
				result.setResultMessage("该订单不存在");
				return result;
			}
			
			if(!orderInfo.getUserId().equals(uid)){
				result.setSuccess(false);
				result.setResultCode("9005");
				result.setResultMessage("您不能取消不属于您的订单");
				return result;
			}
			//订单改为已取消
			orderInfo.setOrderStatus(51);
			orderInfoDao.modify(orderInfo);

			List<OrderDetail> list = orderDetailDao.selectByOrderId(orderInfo.getOrderId());
			for(OrderDetail detail : list){
				skuDao.rollbackSkuStock(detail.getSkuId(), detail.getNum());
			}

			result.setResult(true);
			EcUtils.setSuccessResult(result);
		}catch (Exception e) {
			log.error("", e);
			EcUtils.setExceptionResult(result);
		}
		return result;
	}
	
	/**
	 * 查看该用户是否未生成过订单
	 * @param uid
	 * @return
	 */
	@Override
	public Integer getEffectiveOrderCount(Integer uid) {
		
		try{
			OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
			orderInfoQuery.setUserId(uid);
			
			return this.orderInfoDao.countByCondition(orderInfoQuery);
			
		}catch (Exception e) {
			log.error("", e);
		}
		return null;
		
	}


    private void setBasicOrderInfo(OrderInfo order){
		//默认卖家
		order.setVenderUserId(10000);
		
		//设置收货人地址，使用默认物流收货人信息
		ReceiveAddr addr = receiveAddrService.getDefaultReceiveAddr(order.getUserId());
		order.setConsigneeAddress(addr.getProvinceName()+addr.getCityName()+addr.getCountyName()+addr.getAddress());
		order.setConsigneeCity(addr.getCity());
		order.setConsigneeCounty(addr.getCounty());
		order.setConsigneeId(addr.getId());
		order.setConsigneeMobile(addr.getMobile());
		order.setConsigneeName(addr.getName());
		order.setConsigneeProvince(addr.getProvince());
	}
    
	public void setOrderInfoDao(OrderInfoDao orderInfoDao) {
		this.orderInfoDao = orderInfoDao;
	}

	public void setItemDao(ItemDao itemDao) {
		this.itemDao = itemDao;
	}

	public void setOrderDetailDao(OrderDetailDao orderDetailDao) {
		this.orderDetailDao = orderDetailDao;
	}

	public void setSkuDao(SkuDao skuDao) {
		this.skuDao = skuDao;
	}

	public void setPromotionSkuDao(PromotionSkuDao promotionSkuDao) {
		this.promotionSkuDao = promotionSkuDao;
	}

	public void setPromotionInfoDao(PromotionInfoDao promotionInfoDao) {
		this.promotionInfoDao = promotionInfoDao;
	}

    public void setConsigneeInfoDao(ConsigneeInfoDao consigneeInfoDao) {
        this.consigneeInfoDao = consigneeInfoDao;
    }

    public void setTransactionManager(
			DataSourceTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setUmpInfoDao(UmpInfoDao umpInfoDao) {
		this.umpInfoDao = umpInfoDao;
	}

	public void setUmpInfoService(UmpInfoService umpInfoService) {
		this.umpInfoService = umpInfoService;
	}

	public void setCartService(CartService cartService) {
		this.cartService = cartService;
	}

	public void setReceiveAddrService(ReceiveAddrService receiveAddrService) {
		this.receiveAddrService = receiveAddrService;
	}

	public void setPaymentInfoService(PaymentInfoService paymentInfoService) {
		this.paymentInfoService = paymentInfoService;
	}

	public void setUserInfoDao(UserInfoDao userInfoDao) {
		this.userInfoDao = userInfoDao;
	}

	public void setTaskDao(TaskDao taskDao) {
		this.taskDao = taskDao;
	}

}
