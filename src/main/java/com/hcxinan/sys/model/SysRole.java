package com.hcxinan.sys.model;

import com.hcxinan.core.inte.system.IRole;
import lombok.Data;

import java.io.Serializable;

/**
 * @@Author liudk by 1/8/22 6:39 PM
 */
@Data
public class SysRole implements IRole, Serializable {
    private static final long serialVersionUID = 4591777794300958918L;

    private String roleId;

    private String roleName;
}
