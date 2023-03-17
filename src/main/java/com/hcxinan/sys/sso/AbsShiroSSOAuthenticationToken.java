package com.hcxinan.sys.sso;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author liudk
 * @Description: shiro的登录token
 * @date 21-9-28 下午4:17
 */
public abstract class AbsShiroSSOAuthenticationToken implements AuthenticationToken{

    private String account;//用户账号

    private Object credentials;//凭证

    public AbsShiroSSOAuthenticationToken(String account, Object credentials) {
        this.account = account;
        this.credentials = credentials;
    }

    public abstract boolean isLogined();

    @Override
    public String getPrincipal() {
        return account;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
}
