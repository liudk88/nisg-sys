package com.hcxinan.sys.sso;

import java.util.Map;

/**
 * @author liudk
 * @Description: 单点登陆实现接口
 * @date 21-9-28 上午10:59
 */
public interface ISSO {
    /**
     *@Description 获取单点登录跳转地址(不同的访问地址可能返回的地址是不一样的，如果不需要单点登陆那么返回空
     *@Param [redirectUri:授权后重定向的回调链接地址，如在OAuth2.0中，对应参数redirect_uri]
     *
     *@Return java.lang.String
     *@Author liudk
     *@DateTime 21-9-29 上午10:02
    */
    String getLoginUrl(String redirectUri);

    //由单点登录第三方构造出登录成功的token
    AbsShiroSSOAuthenticationToken getSSOToken(Map param);

    //获取第三方登出地址
    String getLoginOutUrl(String redirectUri);
}
