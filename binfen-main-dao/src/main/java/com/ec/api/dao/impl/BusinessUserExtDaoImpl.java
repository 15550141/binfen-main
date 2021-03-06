package com.ec.api.dao.impl;

import java.util.List;

import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ec.api.dao.BusinessUserExtDao;
import com.ec.api.domain.BusinessUserExt;
import com.ec.api.domain.query.BusinessUserExtQuery;

public class BusinessUserExtDaoImpl extends SqlMapClientTemplate implements BusinessUserExtDao {

	@Override
	public Integer insert(BusinessUserExt businessUserExt) {
		return (Integer) insert("BusinessUserExt.insert", businessUserExt);
	}

	@Override
	public void modifyByUserId(BusinessUserExt businessUserExt) {
		update("BusinessUserExt.updateByUserId", businessUserExt);
	}

	@Override
	public BusinessUserExt selectByUserId(int userId) {
		return (BusinessUserExt) queryForObject("BusinessUserExt.selectByUserId",userId);
	}

	@Override
	public int countByCondition(BusinessUserExtQuery businessUserExtQuery) {
		return (Integer)queryForObject("BusinessUserExt.countByCondition", businessUserExtQuery);
	}

	@Override
	public List<BusinessUserExt> selectByCondition(
			BusinessUserExtQuery businessUserExtQuery) {
		return queryForList("BusinessUserExt.selectByCondition",businessUserExtQuery);
	}

}
