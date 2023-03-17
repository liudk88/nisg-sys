package com.hcxinan.sys.service;

import com.hcxinan.core.inte.message.ISmsMS;
import com.hcxinan.core.inte.system.IUrgeService;
import com.hcxinan.sys.model.SysUrge;
import com.morph.db.IMoDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UrgeServiceImpl implements IUrgeService<SysUrge> {
    @Autowired
    private IMoDao dao;
    @Autowired(required = false)
    private ISmsMS smsMS;//实现发送代办信息服务


    @Override
    public boolean savePushUrges(List<SysUrge> urges,String urge_type) {
        int status=0;//待阅读
        if(smsMS!=null){
            int num=smsMS.batchSendSmsByEntitys(urges);
            if(num>0)status=1;//催办成功
        }

        for(SysUrge sysUrge:urges){
            sysUrge.setStatus(status);
        }
        dao.setBindTable(()->"SYS_URGE").insert(SysUrge.class,urges);
        return status==1;
    }
}
