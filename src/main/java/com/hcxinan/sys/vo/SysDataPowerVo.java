package com.hcxinan.sys.vo;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.hcxinan.core.inte.system.IUser;
import com.hcxinan.sys.model.SysDataPower;

import lombok.Data;
/**
 * 数据权限数据处理对象
 * @author fanlz
 *
 */
@Data
public class SysDataPowerVo {
	
	/**
     * 0：自定义，1，所有业务统一
     */
    private int allBus ;

    /**
     * 业务类型，根据字典(SYS_MODULE)排序，默认为第一个。
     */
    private String btype ;
    /**
     * 范围（枚举PowerScope）
     */
    private int scope = -1 ;
    
    /**
     * 部门树，后端封装成树状json
     */
    private JSONArray  deptList;
    /**
     * 部门树，被选中的节点
     */
    private JSONArray  deptArr;

    /**
     * 区域树，后端封装成树状json
     */
    private JSONArray regionList  ;
    /**
     * 区域树，被选中的节点
     */
    private JSONArray  regionArr;

    /**
     * 区域树，被选中的节点
     */
    private List<SysDataPower>  powers;
    
    /**当前用户*/
    private IUser user;
    
}
