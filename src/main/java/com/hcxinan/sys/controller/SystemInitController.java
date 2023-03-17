package com.hcxinan.sys.controller;

import com.hcxinan.core.inte.system.ISysConfig;
import com.hcxinan.core.util.JsonResult;
import com.hcxinan.sys.util.code.RegisterCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/systeminit")
public class SystemInitController {
    @Autowired(required = false)
    private ISysConfig sysConfig;

    @GetMapping
    public JsonResult getInitMesg(){
        Map<String,Object> result=new HashMap<>();
        if(sysConfig!=null){
            Object offlineInit = sysConfig.getOfflineInit();
            if ("0".equals(offlineInit)) { //0-表示离线端初始化完毕
                /**
                 * 校验离线端是否已经注册
                 * 如果没注册，则将状态改为未初始化
                 */
                RegisterCodeUtil register = new RegisterCodeUtil();
                if (!register.isValidRegisterClient()) {
                    offlineInit = 1; //1-表示离线端未初始化
                }
            }
            result.put("systemName",sysConfig.getSystemName());
            result.put("enableCaptcha",sysConfig.isOpenCaptcha()); // todo sysConfig.enableCaptcha()未实现
//            result.put("enableCaptcha",false); //false：关闭登陆界面验证码，其他情况为开启
            result.put("offlineInit",offlineInit);
            return JsonResult.success(result);
        }
        return JsonResult.error(result);
    }

    @GetMapping("/taskNum")
    public JsonResult getTaskNum(){
//        cacheAttachmentManager.mergeFileToken(fileToken,ids);
        Map<String,Object> result=new HashMap<>();
        int taskNum=sysConfig.getTaskNum();
        result.put("taskNum",taskNum);
        return JsonResult.success(result);
    }
}
