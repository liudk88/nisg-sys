package com.hcxinan.sys.service;

import com.hcxinan.core.inte.system.IOrg;
import com.hcxinan.core.inte.system.IOrgService;
import com.hcxinan.sys.model.SysOrg;
import com.morph.cond.Cond;
import com.morph.db.IMoDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("orgServiceImpl")
public class OrgServiceImpl implements IOrgService {
    private IMoDao deptDao;

    @Override
    public List<SysOrg> getAllOrg() {
        return deptDao.queryBeanList(SysOrg.class, Cond.eq("mark",1),null,null);
    }

    @Override
    public List<Map> getOrgs(long pageSize, long current) {
        return null;
    }

    @Autowired
    public OrgServiceImpl(IMoDao deptDao) {
        this.deptDao = deptDao;
        this.deptDao.setBindTable(()->"sys_dept");
    }
}
