package com.hcxinan.sys.sso;

/**
 * @author liudk
 * @Description: 登录token
 * @date 21-9-28 下午4:17
 */
public interface IToken {
    //获取认证信息，可以是用户id或自定义对象
    Object getPrincipal();
    //获取凭证，可以是密码或自定义的对象
    Object getCredentials();
}
