package com.hcxinan.sys;

import com.morph.db.IConnectionUtil;
import com.morph.db.impl.MoDao;

/**
 * @author liudk
 * @Description:
 * @date 21-7-9 下午8:18
 */
public class MoDaoImpl extends MoDao{

    public void setConUtil(IConnectionUtil conUtil) {
        super.setConUtil(conUtil);
    }
}
