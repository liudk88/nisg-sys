package com.hcxinan.sys.cache;

import com.hcxinan.core.inte.system.ISysRule;
import com.hcxinan.core.inte.system.ISysRuleMs;
import com.hcxinan.sys.cache.inte.ICacheCode;
import com.hcxinan.sys.cache.inte.ISystemCache;
import com.hcxinan.sys.service.ICommonService;
import com.hcxinan.sys.util.PlatUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: (数据字典缓存管理器,对系统中的常用数据都字典缓存,统一管理)
 * @Author: Fly
 * @Date: 2021/7/23 - 10:47
 * @Description:
 */
@Component("cacheManager")
@DependsOn("initDb")
public class CacheManager implements ISystemCache, ISysRuleMs {

    private final static Map<String, Map<String,ICacheCode>> caches =new HashMap<>();

    private static CacheManager cacheManager = null;

    private static final Logger log = Logger.getLogger(CacheManager.class);
    @Autowired
    private ICommonService commonService;

    public static CacheManager getInstance() {
        if (cacheManager == null) {
            cacheManager = (CacheManager) PlatUtil.getApplicationContext().getBean("cacheManager");
        }
        return cacheManager;
    }

    @PostConstruct
    public void load(){
        load(null);
    }

    private synchronized void load(String cacheCode){
        try{
            List<ICacheCode> list = commonService.queryList("plat.codeCache", cacheCode);
            if(list != null){
                for (ICacheCode code : list) {
                    Map<String,ICacheCode> cacheCodeMap= caches.get(code.getCode());
                    if(cacheCodeMap==null){
                        cacheCodeMap=new HashMap<>();
                        caches.put(code.getCode(),cacheCodeMap);
                    }
                    cacheCodeMap.put(code.getKey(),code);
                }
            }



        }catch(Exception e){
            e.printStackTrace();
        }


    }

    public void reload() {
        release();
        load();
    }

    @Override
    public Map<String, ICacheCode> getRuleMaps(String ruleName) {
        return caches.get(ruleName);
    }

    @Override
    public List<ISysRule> getRules(String ruleName) {
        return getCodes(ruleName);
    }

    @Override
    public ISysRule getRule(String ruleName, String key) {
        return getCode(ruleName,key);
    }

    public void reload(String cacheCode)  {
        if (cacheCode != null && "".equals(cacheCode) == false) {
            caches.remove(cacheCode);
            load(cacheCode);
        }else{
            reload();
        }
    }

    public List getCodes(String key) {
        Map<String,ICacheCode> codes=caches.get(key);
        if(codes!=null){
            return new ArrayList(caches.get(key).values());
        }else{
            return null;
        }
    }

    public ICacheCode getCode(String code, String subcode) {
        Map<String,ICacheCode> map=caches.get(code);
        if(map==null){
            throw new NullPointerException("没有找到健为"+code+"的字典，请检查字典表中是否存在该数据！");
        }
        return map.get(subcode);
    };
    /**
     *
     * @param code
     * @param name
     * @return
     */
    public ICacheCode getCodeByName(String code, String name) {
        List<ICacheCode> codes = getCodes(code);
        if(null!=codes){
            for (ICacheCode c : codes) {
                if (c.getCname().equals(name) == true) {
                    return c;
                }
            }
        }
        return null;
    };


    public void remove(String cacheCode) {
        caches.remove(cacheCode);
    }

    public void release()  {
        caches.clear();
    }

    public String getDescription() {
        return "BaseMapper.xml{menuCache} :字典缓存，字典键绑定{CODES},部门信息绑定{ORGS}，权限信息绑定{PERMISSION}，系统信息绑定{SYSTEMS}";
    }
}
