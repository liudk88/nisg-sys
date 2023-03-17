package com.hcxinan.sys.controller;

import com.hcxinan.core.inte.system.ISysUtil;
import com.hcxinan.core.inte.system.IUrgeService;
import com.hcxinan.core.inte.system.IUser;
import com.hcxinan.core.util.JsonResult;
import com.hcxinan.sys.model.SysUrge;
import com.morph.db.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
  * @Description: 任务催办控制器
  *
  * @author: liudk
  * @date:  2021-12-16 20:35
  */
@RestController
@RequestMapping("/urge")
public class UrgeController {
    //系统工具
    @Autowired(required = false)
    private ISysUtil sysUtil;
    @Autowired
    private IUrgeService urgeService;

    @RequestMapping(value = "/sms",method = {RequestMethod.POST})
    public JsonResult urgeSms(@RequestBody Map<String,Object> params){
        try {
            String btype= (String) params.get("btype");
            String business_id= (String) params.get("business_id");
            String param1= (String) params.get("param1");
            String param2= (String) params.get("param2");
            String param3= (String) params.get("param3");
            String title= (String) params.get("title");

            //todo: 消息内容，应该用接口定义
            String content = "【安管平台】当前平台接收到待办工单任务："+ title +"，请及时登录平台进行处理。";
            List<String> userIds= (List<String>) params.get("userIds");
            List<SysUrge> saveDatas=new ArrayList<>();
            for(String uid:userIds){
                IUser user=sysUtil.getLoginUser();

                SysUrge sysUrge=new SysUrge();
                sysUrge.setUrge_id(IDGenerator.getDateTimeId());
                sysUrge.setBtype(btype);
                sysUrge.setBusiness_id(business_id);
                sysUrge.setPuser(sysUtil.getLoginUser().getId());
                sysUrge.setUrged_man(uid);
                sysUrge.setContent(content);
                if(user.getTels()!=null && user.getTels().size()>0){
                    sysUrge.setPhone(user.getTels().get(0));
                }
                sysUrge.setEmail(user.getEmail());
                sysUrge.setUrge_type("0");
                sysUrge.setStatus(0);
                sysUrge.setCdate(new Date());
                sysUrge.setCreator(sysUtil.getLoginUser().getId());
                sysUrge.setValid(true);
                sysUrge.setParam1(param1);
                sysUrge.setParam2(param2);
                sysUrge.setParam3(param3);

                saveDatas.add(sysUrge);
                boolean flag=urgeService.savePushUrges(saveDatas,"0");
                if(flag){
                    return JsonResult.success();
                }else{
                    return JsonResult.error();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.error();
    }
}
