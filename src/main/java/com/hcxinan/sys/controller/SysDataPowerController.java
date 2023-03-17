package com.hcxinan.sys.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hcxinan.core.util.JsonResult;
import com.hcxinan.sys.model.SysDataPower;
import com.hcxinan.sys.service.IDataPowerBussinessService;
import com.hcxinan.sys.vo.SysDataPowerVo;

/**
 * @author fanlz
 * @Description: 用户或角色的数据权限控制器
 * @date 22-2-28 下午6:55
 */
@RestController
@RequestMapping("/datapower")
public class SysDataPowerController {

    @Autowired
    private IDataPowerBussinessService dataPowerService;

    @PostMapping()
    public JsonResult savePowerList(@RequestBody SysDataPowerVo vo){
        return JsonResult.success(dataPowerService.savePowerList(vo.getPowers(),vo.getBtype()));
    }
  

    @GetMapping("/role/{rolecode}")
    public JsonResult getRoleDataPower(@PathVariable("rolecode") String rolecode,@RequestParam(name="btype",required = false)String btype){
        return JsonResult.success(dataPowerService.getDataPowerByRoleOrUser(rolecode, btype, "role"));
    }
    
    
    @GetMapping("/user/{userid}")
    public JsonResult getUserDataPower(@PathVariable("userid") String userid,@RequestParam(name="btype",required = false)  String btype){
        return JsonResult.success(dataPowerService.getDataPowerByRoleOrUser(userid, btype, "user"));
    }
}
