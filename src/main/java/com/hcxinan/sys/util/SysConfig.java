package com.hcxinan.sys.util;

import com.hcxinan.core.inte.system.ISysConfig;
import com.hcxinan.core.inte.system.ISysRule;
import com.hcxinan.core.inte.system.ISysRuleMs;
import com.morph.db.DbType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class SysConfig implements ISysConfig {
    @Autowired(required = false)
    private ISysRuleMs sysRuleMs;

    @Value("${morph.dbType}")
    private DbType dbType;

    @Override
    public boolean enableAutoUpdateDb() {
        return true;
    }

    @Override
    public String getSystemName() {
        return getRuleVal("SYSTEM_NAME");
    }

    @Override
    public String getSystemLogo() {
        return getRuleVal("SYSTEM_LOGO");
    }

    @Override
    public String getDbType() {
        return DbType.DMdb.toString();
    }

    @Override
    public String getOfflineInit() {
        return getRuleVal("offlineInit");
    }

    @Override
    public boolean isOpenCaptcha() {
        String captchAbledStr=getRuleVal("CaptchAbled");
        if("0".equals(captchAbledStr)){//关闭了验证码功能
            return false;
        }else{
            return true;
        }
    }

    @Override
    public String getLayoutStyle() {
        return getRuleVal("layoutStyle");
    }

    private String getRuleVal(String key){
        AtomicReference<String> sysName= new AtomicReference<>("");
        getSystemSettingVal(key,rule -> sysName.set(rule.getVal()));
        return sysName.get();
    }

    private void getSystemSettingVal(String key, Consumer<ISysRule> consumer){
        if(sysRuleMs!=null){
            ISysRule sysRule = sysRuleMs.getRule("SYSCONFIG",key);
            if(sysRule!=null){
                consumer.accept(sysRule);
            }
        }
    }
}
