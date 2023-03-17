package com.hcxinan.sys.inte;

import com.hcxinan.core.inte.system.IUser;
import org.springframework.stereotype.Component;

/**
*@Description 系统信息接口,可以获取到系统一些基本信息
*@Param 
*@Return 
*@Author liudk
*@DateTime 20-12-30 下午7:47
*/
@Component
public interface ISystemMsg {
    //获取当前登录用户
    IUser getLoginUser();
}
