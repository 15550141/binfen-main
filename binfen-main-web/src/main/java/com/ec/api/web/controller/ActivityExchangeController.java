package com.ec.api.web.controller;

import com.ec.api.common.utils.CookieUtils;
import com.ec.api.dao.ActivityExchangeDao;
import com.ec.api.domain.ActivityExchange;
import com.ec.api.domain.query.ActivityExchangeQuery;
import com.ec.api.service.result.Result;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/activityExchange")
public class ActivityExchangeController {

	@Autowired
	private ActivityExchangeDao activityExchangeDao;

	private final static Log log = LogFactory.getLog(ActivityExchangeController.class);

	@RequestMapping(value="/insert", method={ RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody
	Result insert(ActivityExchange activityExchange, HttpServletResponse response, HttpServletRequest request, ModelMap content) {
		Result result = new Result();
		try{
			ActivityExchangeQuery query = new ActivityExchangeQuery();
			query.setUid(CookieUtils.getUid(request));
			query.setActivityId(0l);//TODO 以后要修改，这个第一次默认为0

			List<ActivityExchange> list = this.activityExchangeDao.selectByCondition(query);
			if(list != null && list.size() > 0){
				result.setSuccess(false);
				result.setResultMessage("该用户手机号已经领取过奖品！");
				return result;
			}

			Date date = new Date();
			activityExchange.setUid(CookieUtils.getUid(request));
			activityExchange.setActivityId(0l);
			activityExchange.setActivityName("集赞领奖品");
			activityExchange.setApplyTime(date);
			activityExchange.setCreated(date);
			activityExchange.setModified(date);
			activityExchange.setStatus(0);

			this.activityExchangeDao.insert(activityExchange);
			result.setSuccess(true);
		}catch (Exception e){
			result.setSuccess(false);
			log.error("", e);
		}
		return result;
	}

	@RequestMapping(value="/add", method={ RequestMethod.GET, RequestMethod.POST })
	public String add(HttpServletResponse response, HttpServletRequest request, ModelMap content) {

		ActivityExchangeQuery query = new ActivityExchangeQuery();
		query.setUid(CookieUtils.getUid(request));
		query.setActivityId(0l);//TODO 以后要修改，这个第一次默认为0

		List<ActivityExchange> list = this.activityExchangeDao.selectByCondition(query);
		if(list != null && list.size() > 0){
			content.put("detail", list.get(0));
			return "activityExchange/done";
		}

		return "activityExchange/add";
	}

}

