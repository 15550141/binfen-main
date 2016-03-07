package com.ec.api.web.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ec.api.domain.Item;
import com.ec.api.domain.query.ItemQuery;
import com.ec.api.service.ItemService;
import com.ec.api.service.result.Result;
import com.ec.api.web.base.BaseController;

@Controller
@RequestMapping("item")
public class ItemController extends BaseController {
	
	private static final Logger log = LoggerFactory.getLogger(ItemController.class);

	@Autowired
	private ItemService itemService;
	
	@RequestMapping(value="getItemsByVenderUserId", method={RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody Result getItemsByVenderUserId(HttpServletRequest request,HttpServletResponse response, ModelMap context){
		return this.itemService.getItemsByVenderUserId(getUserId(request));
	}
	
	
	@RequestMapping(value="updateItem", method={RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody Result updateItem(Item item, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		Result result = new Result();
		if(item.getItemId() == null){
			result.setResultCode("1001");
			result.setResultMessage("itemId不能为空");
			return result;
		}
		item.setModified(new Date());
		return this.itemService.updateItem(item);
	}
	
	@RequestMapping(value="getItemImages", method={RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody Result getItemImages(Integer itemId, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		if(itemId == null){
			Result result = new Result();
			result.setResultCode("1001");
			result.setResultMessage("itemId不能为空");
		}
		return this.itemService.getItemImages(itemId);
	}

	@RequestMapping(value="detail", method={RequestMethod.GET, RequestMethod.POST})
	public String detail(String id, String skuId, HttpServletRequest request,HttpServletResponse response, ModelMap context){
		if(id == null){
			return "";
		}
		try{
			Integer itemId = Integer.parseInt(id);
			Item item = itemService.getItemByItemId(itemId);
			if(item != null){
				context.put("item", item);
				if(StringUtils.isNotBlank(skuId)){
					context.put("skuId", Integer.parseInt(skuId));
				}
				return "item/detail";
			}
		}catch (Exception e) {
			log.error("", e);
		}

		return "error";
	}

	@RequestMapping(value="all", method={RequestMethod.GET, RequestMethod.POST})
	public String all(HttpServletRequest request,HttpServletResponse response, ModelMap context){
		try{
			List<Item> list = itemService.getAll();
			context.put("list", list);
			return "item/all";
		}catch (Exception e) {
			log.error("", e);
		}
		return "error";
	}

	/**
	 * 所有水果
	 * @param request
	 * @param response
	 * @param context
	 * @return
	 */
	@RequestMapping(value="allsg", method={RequestMethod.GET, RequestMethod.POST})
	public String allsg(HttpServletRequest request,HttpServletResponse response, ModelMap context){
		try{
			ItemQuery query = new ItemQuery();
			query.setItemStatus(1);//上架
			query.setCategoryId2(12);//水果
			List<Item> list = itemService.getAll(query);
			context.put("list", list);
			return "item/all";
		}catch (Exception e) {
			log.error("", e);
		}
		return "error";
	}

	/**
	 * 所有干果
	 * @param request
	 * @param response
	 * @param context
     * @return
     */
	@RequestMapping(value="allgg", method={RequestMethod.GET, RequestMethod.POST})
	public String allgg(HttpServletRequest request,HttpServletResponse response, ModelMap context){
		try{
			ItemQuery query = new ItemQuery();
			query.setItemStatus(1);//上架
			query.setCategoryId2(22);//水果
			List<Item> list = itemService.getAll(query);

			ItemQuery query2 = new ItemQuery();
			query2.setItemStatus(1);//上架
			query2.setCategoryId2(49);//水果
			List<Item> list2 = itemService.getAll(query2);

			list.addAll(list2);
			context.put("list", list);
			return "item/all";
		}catch (Exception e) {
			log.error("", e);
		}
		return "error";
	}
	

}
