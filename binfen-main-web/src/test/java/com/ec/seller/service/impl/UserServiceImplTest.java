package com.ec.seller.service.impl;

import com.ec.BaseTest;
import com.ec.api.common.utils.FlagBitUtil;
import com.ec.api.common.utils.PropertyConstants;
import com.ec.api.dao.UserInfoDao;
import com.ec.api.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by yujianming on 2016/1/12.
 */
public class UserServiceImplTest extends BaseTest {

    @Autowired
    private UserInfoDao userInfoDao;

    @org.junit.Test
    public void testGetUserInfo() throws Exception {
        UserInfo userInfo = userInfoDao.selectByUserId(10000);
        System.out.println(userInfo.toString());
    }

    @org.junit.Test
    public void testQueryUser() throws Exception {
        UserInfo userInfo = userInfoDao.selectByUserId(10000);
        Integer property = userInfo.getProperties();
        if(property == null ){
            property = 0;
        }
        userInfo.setProperties(FlagBitUtil.sign(property, PropertyConstants.USER_FENXIAOSHANG));
        userInfoDao.modify(userInfo);
        System.out.println("成功");
    }

}