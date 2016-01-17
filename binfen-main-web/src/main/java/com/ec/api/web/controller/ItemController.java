package com.ec.api.web.controller;

import java.util.Date;

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
	

}
