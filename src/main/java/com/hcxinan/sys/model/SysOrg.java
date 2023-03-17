package com.hcxinan.sys.model;

import com.hcxinan.core.inte.system.IOrg;
import lombok.Data;

import java.io.Serializable;

/**
 * @author liudk
 * @Description:
 * @date 21-9-24 下午7:24
 */
@Data
public class SysOrg implements IOrg, Serializable {

    private static final long serialVersionUID = -8512396849351794158L;

    private String id;

    private String name;

    private String uscc;

    @Override
    public String getOrgId() {
        return id;
    }

    @Override
    public String getOrgName() {
        return name;
    }
}
