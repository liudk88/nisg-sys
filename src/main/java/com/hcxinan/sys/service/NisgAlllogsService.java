package com.hcxinan.sys.service;

import com.hcxinan.sys.mapper.NisgAlllogsDao;
import com.hcxinan.sys.model.NisgAlllogs;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class NisgAlllogsService {

    @Resource
    private NisgAlllogsDao nisgAlllogsDao;


    public  void insertLog(NisgAlllogs bean){
        nisgAlllogsDao.insert(bean);
    }
}
