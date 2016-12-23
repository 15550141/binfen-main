package com.ec.api.web.controller;

import com.ec.api.common.utils.CookieUtils;
import com.ec.api.dao.CooperationBusinessDao;
import com.ec.api.domain.CooperationBusiness;
import com.ec.api.domain.query.CooperationBusinessQuery;
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
@RequestMapping("/cooperationBusiness")
public class CooperationBusinessController {

	@Autowired
	private CooperationBusinessDao cooperationBusinessDao;

	private final static Log log = LogFactory.getLog(CooperationBusinessController.class);

	@RequestMapping(value="/insert", method={ RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody
	Result insert(CooperationBusiness cooperationBusiness, HttpServletResponse response, HttpServletRequest request, ModelMap content) {
		Result result = new Result();
		try{
			CooperationBusinessQuery query = new CooperationBusinessQuery();
			query.setUid(CookieUtils.getUid(request));

			List<CooperationBusiness> list = this.cooperationBusinessDao.selectByCondition(query);
			if(list != null && list.size() > 0){
				result.setSuccess(false);
				result.setResultMessage("您已经申请过该合作伙伴！");
				return result;
			}

			Date date = new Date();
			cooperationBusiness.setUid(CookieUtils.getUid(request));
			cooperationBusiness.setCreated(date);
			cooperationBusiness.setModified(date);
			cooperationBusiness.setStatus(0);

			this.cooperationBusinessDao.insert(cooperationBusiness);
			result.setSuccess(true);
		}catch (Exception e){
			result.setSuccess(false);
			log.error("", e);
		}
		return result;
	}

	@RequestMapping(value="/add", method={ RequestMethod.GET, RequestMethod.POST })
	public String add(HttpServletResponse response, HttpServletRequest request, ModelMap content) {

		CooperationBusinessQuery query = new CooperationBusinessQuery();
		query.setUid(CookieUtils.getUid(request));

		List<CooperationBusiness> list = this.cooperationBusinessDao.selectByCondition(query);
		if(list != null && list.size() > 0){
			content.put("detail", list.get(0));
			return "cooperationBusiness/done";
		}

		return "cooperationBusiness/add";
	}

}

