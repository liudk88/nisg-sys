package com.hcxinan.sys.filter;

//import com.hcxinan.sys.formVew.SysUser;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
*@Description 登录过滤器,目的是把没有通过登录认证的请求重定向到登录页面
*@Param 
*@Return 
*@Author liudk
*@DateTime 19-9-19 下午1:36
*/
public class LoginFilter implements Filter {

    private static final Logger log = Logger.getLogger(LoginFilter.class);
    private static String loginPage = null;//拦截后重定向的登录页面
    private static Set<String> excludeURL = Collections.EMPTY_SET;//不需要过滤的路径


    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String ctx = request.getContextPath();
        String uri = request.getRequestURI();
        uri = uri.substring(ctx.length(), uri.length());

        if (acceptURI(uri)) {
            chain.doFilter(request, response);
            return;
        } else {
            if (isLogin(request)) {
                chain.doFilter(request, response);// 如果用户已经登录，就放行
                return;
            } else {
                response.sendRedirect(ctx+loginPage);
                return;
            }
        }
    }

    public void init(FilterConfig config) throws ServletException {
        String urls = config.getInitParameter("excludeURL");
        log.info("需要忽略的访问配置：" + urls);
        if (!StringUtils.isEmpty(urls)) {
            String arr[] = urls.split(",");
            if (arr != null) {
                excludeURL=new HashSet<String>(Arrays.asList(arr));
            }
        }
        loginPage = config.getInitParameter("loginPage");
        loginPage=loginPage==null?"":loginPage;
    }

    public void destroy() {

    }
    /**
    *@Description 检查当前访问的地址是否是不需要登录认证的
    *@Param [uri]
    *@Return boolean
    *@Author liudk
    *@DateTime 19-9-18 下午2:02
    */
    private boolean acceptURI(String uri) {
        if(uri.equals(loginPage)){//如果就是需要重定向的登录地址,那么也是不用拦截的,否则会出现死循环
            return true;
        }
        //忽略匹配开头的文件路径
        outer:for(String curi:excludeURL){
            if(uri.startsWith(curi)){
                return true;
            }
        }
        return false;
    }
    /**
    *@Description 判断是否已经登录过系统
    *@Param [request]
    *@Return boolean
    *@Author liudk
    *@DateTime 19-9-18 下午2:04
    */
    private boolean isLogin(HttpServletRequest request){
        /*SysUser user= (SysUser) request.getSession().getAttribute("_SESSION_USER");
        if(user!=null){
            return true;
        }else{
            return false;
        }*/
        return true;
    }
}
