package com.hcxinan.sys.model;

import com.hcxinan.core.inte.system.IOrg;
import com.hcxinan.core.inte.system.IRole;
import com.hcxinan.core.inte.system.IUser;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liudk
 * @Description:
 * @date 21-9-24 下午5:50
 */
@Data
public class SysUser implements IUser, Serializable {

    private static final long serialVersionUID = -8236935852522487117L;

    private String id;

    private String account;

    private String name;

    private String orgId;

    private List<String> tels;

    private String email;
    
    private String password;

    private SysOrg org;
    //存放一些扩展信息
    private Map<String,Object> params = new HashMap<>(16);

    private List<IRole> roles;

    @Override
    public Object getParam(String key) {
        return params.get(key);
    }

    public void put(String key,Object val){
        params.put(key,val);
    }
}
