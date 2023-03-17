package com.hcxinan.sys.util;

import com.morph.db.IConnectionUtil;
import com.morph.db.impl.MoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author liudk
 * @Description:
 * @date 21-7-9 下午8:18
 */
@Component("moDaoImpl")
@Scope("prototype")
public class MoDaoImpl extends MoDao{

    @Autowired
    @Override
    public void setConUtil(IConnectionUtil conUtil) {
        super.setConUtil(conUtil);
    }
}
