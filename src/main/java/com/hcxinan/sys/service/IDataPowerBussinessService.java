package com.hcxinan.sys.service;

import java.util.List;

import com.hcxinan.sys.model.SysDataPower;
import com.hcxinan.sys.vo.SysDataPowerVo;

public interface IDataPowerBussinessService {

		boolean savePowerList(List<SysDataPower> powers,String id);
		
		/**
		 * 根据用户id或者角色id获取数据权限信息
		 * @param id  用户id或角色id
		 * @param power_type org/user
		 * @return
		 */
		SysDataPowerVo getDataPowerByRoleOrUser(String id,String btype,String power_type);
}
