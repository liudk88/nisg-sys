package com.hcxinan.sys.schedule;

import com.morph.db.ITrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author liudk
 * @Description:
 * @date 21-9-30 上午11:41
 */
@Component
public class SchedulerTrigger implements ITrigger<Map<String,Object>> {
    @Autowired
    private ISchedulerManager schedulerManager;

    @Override
    public Object doBeforeInsert(List<Map<String, Object>> list) {
        return null;
    }

    @Override
    public Object doAfterInsert(List<Map<String, Object>> list) {
        if(list!=null){
            Map<String, Object> data=list.get(0);
            if(data!=null){
                String pkv= (String) data.get("pkv");
                String enabled= (String) data.get("ENABLED");
                if("1".equals(enabled)){//启用
                    try {
                        schedulerManager.addJob(pkv);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Object doBeforeUpdate(List<Map<String, Object>> list) {
        return null;
    }

    @Override
    public Object doAfterUpdate(List<Map<String, Object>> list) {
        if(list!=null){
            Map<String, Object> data=list.get(0);
            if(data!=null){
                String pkv= (String) data.get("pkv");
                String enabled= (String) data.get("ENABLED");
                if("0".equals(enabled)){//禁用
                    try {
                        schedulerManager.stop(pkv);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }else{//启用
                    String cronStr= (String) data.get("CRON");
                    schedulerManager.updateCron(pkv,cronStr);
                }
            }
        }
        return null;
    }

    @Override
    public Object doBeforeDelete(Serializable... serializables) {
        return null;
    }

    @Override
    public Object doAfterDelete(Serializable... serializables) {
        for(Serializable id: serializables){
            try {
                schedulerManager.stop((String) id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Object doBeforeSelect(Serializable serializable) {
        return null;
    }

    @Override
    public Object doAfterSelect(Map<String, Object> stringObjectMap) {
        return null;
    }

    @Override
    public Function<String, Object> doBeforeQuery(List<String> list) {
        return null;
    }

    @Override
    public Object doAfterQuery(List<Map> list) {
        return null;
    }
}
