package com.hcxinan.sys.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.hcxinan.core.inte.system.IUser;
import com.hcxinan.core.inte.system.IUserService;
import com.hcxinan.core.util.JsonResult;
import com.hcxinan.sys.cache.CacheManager;
import com.hcxinan.sys.constant.CodeType;
import com.hcxinan.sys.inte.ICodeUtil;
import com.hcxinan.sys.model.SysUser;
import com.hcxinan.sys.sso.AbsShiroSSOAuthenticationToken;
import com.hcxinan.sys.sso.ISSO;
import com.hcxinan.sys.util.CodeFactory;
import com.hcxinan.sys.util.MD5Util;
import com.morph.cond.Cond;

/**
 * @author liudk
 * @Description:
 * @date 21-9-28 上午9:59
 */
@RestController("userControllerNisg")
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    //特殊字符，键盘上能输入的英文符号
    private static final String specialChars = "~!@#$%^&*()_+`-=[]{};':,./<>?|\\\"";

    @Autowired(required = false)
    private ISSO isso;

    @Autowired
    private IUserService userService;

    /**
     * @Description 获取单点登录的地址，如果没有则代表不需要开启单点登录，如果有则返回地址
     * @Param
     * @Return com.hcxinan.core.util.JsonResult
     * @Author liudk
     * @DateTime 21-9-28 上午10:01
     */
    @GetMapping("/sso")
    public JsonResult sso(HttpServletRequest request) {
        String visitUrl = request.getParameter("visitUrl");//当前浏览器访问地址
        String hashVal=request.getParameter("hashVal");//获取URL中的锚点
        if(StringUtils.isNotBlank(hashVal)){//前端地址是hash方式
            visitUrl+="?hashVal="+hashVal;
        }
        String ssoUrl = null;
        if (isso != null) {
            ssoUrl = isso.getLoginUrl(visitUrl);
        }
        return JsonResult.success(ssoUrl);
    }

    /**
     * @Description 通过单点登录的方式登录系统。单点登录大致流程：
     * 1.系统判断用户是否已经登录。（有则放行）
     * 2.没有登录，判断是否开启了单点登录（没有，直接走正常的系统登录流程即可）
     * 3.系统跳转到第三方系统登录页面。（前端跳转）
     * 4.第三方系统完成用户登录。
     * 5.第三方系统回跳到我们系统（用前端和后端地址应该都可以，但如果是前后端分离的情况
     * ，估计跳转到前端好一点，因为如果是后端，那么自己的后端还要考虑再次跳转到前端。）
     * 第三方系统回跳地址肯定会带有参数，用来告诉我们登录情况
     * 6.如果第三方登录成功，那么我们系统将要对用户通过登录认证（也就是本方法）
     * @Param []
     * @Return com.hcxinan.core.util.JsonResult
     * @Author liudk
     * @DateTime 21-9-29 上午8:17
     */
    @PostMapping("/sso/login")
    public JsonResult login(@RequestBody Map<String, Object> params) {
        if (isso != null) {
            // 验证身份和登陆
            Subject subject = SecurityUtils.getSubject();
            AbsShiroSSOAuthenticationToken token = isso.getSSOToken(params);
            // 进行登录操作
            subject.login(token);

            String access_token = SecurityUtils.getSubject().getSession().getId().toString();
            return JsonResult.success(access_token);
        }
        return JsonResult.success();
    }

    /**
     * 接口地址：/user/init
     * 1：根据单位初始化帐号
     * 2：离线端注册
     * @param params
     * @return
     */
    @PostMapping("/init")
    public JsonResult getInitUser(@RequestBody Map<String, Object> params) throws Exception {
        if (!params.containsKey("unitUnifyCode")) {
            throw new NullPointerException("统一社会认证编码不能为空");
        }
        if (!params.containsKey("license")) {
            throw new NullPointerException("离线端授权码不能控为空");
        }
        //初始化账号的密码
        String password = ""+params.get("password");
        //先校验密码是否符合规则
        if (!pwdIsComply(password)) return errResult(ErrType.PWD_NON_COMPLIANCE);

        //统一社会认证编码
        String unitUnifyCode = params.get("unitUnifyCode").toString();
        if (StringUtils.isBlank(unitUnifyCode)) return errResult(ErrType.USCC_ACCOUNT_NON_EXIST);

        //离线端授权码
        String license = params.get("license").toString();
        //查询条件
        Cond cond = Cond.eq("USCC", unitUnifyCode);
        //查询对应的账号
        List<? extends IUser> users = userService.queryUsers(null, cond);
        if (users.size() > 0) {
            IUser user = users.get(0);
            log.info("社会认证编码正确，对应的初始化账号数据==>" + JSONObject.toJSONString(user));
            ICodeUtil codeUtil = CodeFactory.getCodeUtil(CodeType.CHECK); //校验码工具类
            String account = user.getAccount(); //初始化的账号
            String checkCode = codeUtil.getCode(account); //根据账号生成的校验码
            ICodeUtil register = CodeFactory.getCodeUtil(CodeType.REGISTER);
            if (register.verify(license, null)) {
                JsonResult result = updatePwd(account, checkCode, CodeType.CHECK.getUnique(), password);
                if (result.getCode() == 0) {
                    return JsonResult.success(account);
                } else {
                    return result;
                }
            } else {
                return errResult(ErrType.LICENSE_CODE_INCORRECT);
            }
        } else {
            return errResult(ErrType.USCC_ACCOUNT_NON_EXIST);
        }
    }

    /**
     * 根据验证码更新密码，密码须符合系统指定规则，验证码正确
     * @param account 账号
     * @param checkCode 验证码
     * @param codeType 验证码类型-CodeType.unique
     * @param password 密码
     * @return
     */
    @PostMapping("/updatePwd")
    public JsonResult updatePwd(String account, String checkCode, String codeType, String password) {
        CodeType type = null;
        ICodeUtil codeUtil = null;
        try {
            List<IUser> users = userService.getUsersByAccount(account);
            if (users.size() >= 0) {
                type = CodeType.getInstance(codeType);
                codeUtil = CodeFactory.getCodeUtil(type);
                if (codeUtil.verify(checkCode, account)) {
                    if (pwdIsComply(password)) {
                        Map<String, Object> map = new HashMap<String, Object>() {{
                            put("id", users.get(0).getId());
                            put("password", MD5Util.passwordEncrypt(password));
                            put("param1","1");
                            put("ID", users.get(0).getId());
                            put("PASSWORD", MD5Util.passwordEncrypt(password));
                            put("PARAM1","1");
                        }};
                        userService.updateUser(map);
                        return JsonResult.success();
                    } else return errResult(ErrType.PWD_NON_COMPLIANCE);
                } else return errResult(ErrType.VERIFY_CODE_INCORRECT);
            }
        } catch (Exception e) {
            if (e instanceof StringIndexOutOfBoundsException) {
                return errResult(ErrType.ACCOUNT_NON_EXIST);
            }
            e.printStackTrace();
        }
        return JsonResult.error();
    }
    /**
     * 初次登录，强制更新密码，密码须符合系统指定规则，
     * @param account 账号
     * @param password 密码
     * @return
     */
    @PostMapping("/editPwd")
    public JsonResult editPwd(String account, String oldPwd, String password) {
        try {
            List<IUser> users = userService.getUsersByAccount(account);
            if (users.size() >= 0) {
            	SysUser user = (SysUser)users.get(0);
            	boolean flag = MD5Util.passwordEncrypt(oldPwd).equals(user.getPassword()) ;
                if (flag) {
                    if (pwdIsComply(password)) {
                        Map<String, Object> map = new HashMap<String, Object>() {{
                            put("ID", users.get(0).getId());
                            put("PASSWORD", MD5Util.passwordEncrypt(password));
                            put("param1","1");
                        }};
                        userService.updateUser(map);
                        return JsonResult.success();
                    } else return errResult(ErrType.PWD_NON_COMPLIANCE);
                } else return JsonResult.error("账号密码错误");
            }
        } catch (Exception e) {
            if (e instanceof StringIndexOutOfBoundsException) {
                return errResult(ErrType.ACCOUNT_NON_EXIST);
            }
            e.printStackTrace();
        }
        return JsonResult.error();
    }
    /**
     * 忘记密码初始化参数
     * @return
     */
    @GetMapping("/resetPwdInit")
    public JsonResult resetPwdInit() {
        Map<String, Object> map = new HashMap();
        //重置密码的方式，以json格式配置；case：{"check":"check","sms":"sms","mail":"mail"}
        //重置密码的方式，以json格式配置；case：{"check":"平台校验码","sms":"短信验证码","mail":"邮件验证码"}
        String mode = getDictValue("RESET_PWD", "MODE");
        if (StringUtils.isNotBlank(mode)) {
            map.put("mode", JSONObject.parseObject(mode));
        }
        //case: 说明：“校验码”需与管理员联系获取，有效期仅为当天有效，如果获取请致电：0871-1234-5678
        String tips = getDictValue("RESET_PWD", "TIPS");
        if (StringUtils.isNotBlank(tips)) {
            map.put("tips", tips);
        }
        return JsonResult.success(map);
    }

    @GetMapping("/account/{accountId}")
    public JsonResult queryByAccount(@PathVariable String accountId) {
        Map<String, Object> map = new HashMap();
        List<IUser> users=userService.getUsersByAccount(accountId);
        if(users==null || users.isEmpty()){
            map.put("code",0);
            map.put("msg","帐号不存在！");
        }else{
            map.put("code",1);
            map.put("data",users);
        }
        return JsonResult.success(map);
    }

    @PostMapping("/checkCode")
    public JsonResult checkCode(@RequestParam("account") String account) throws Exception {
        ICodeUtil codeUtil = CodeFactory.getCodeUtil(CodeType.CHECK);
        String code = codeUtil.getCode(account);
        return JsonResult.success(code);
    }

    /**
     * 校验密码是否符合规则
     * @param pwd
     * @return
     */
    boolean pwdIsComply(String pwd) {
        Pattern lower = Pattern.compile(".*[a-z].*");
        Pattern upper = Pattern.compile(".*[A-Z].*");
        Pattern special = Pattern.compile(String.format(".*[%s].*", Pattern.quote(specialChars)));
        if (StringUtils.isNotBlank(pwd) //不为空字符串
                && !pwd.contains(" ") //不能包含空格
                && pwd.length() >= 10 //长度在10位或以上
                && upper.matcher(pwd).matches() //包含大写字母
                && lower.matcher(pwd).matches() //包含小写字母
                && special.matcher(pwd).matches()) { //包含特殊字符
            return true;
        }
        return false;
    }

    /**
     * 获取系统字段值
     * @param code
     * @param subCode
     * @return
     */
    String getDictValue(String code, String subCode) {
        try {
            if (CacheManager.getInstance().getCodes(code).size() >= 0) {
                return CacheManager.getInstance().getCode(code, subCode).getCname();
            }
        } catch (NullPointerException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 构建异常 JsonResult
     * @param err
     * @return
     */
    JsonResult errResult(ErrType err) {
        return JsonResult.error(err.code, err.msg);
    }

    /**
     * 当前类接口的异常返回信息枚举
     */
    static enum ErrType {
        PWD_NON_COMPLIANCE(1, "密码不符合规则，请检查是否按要求正确输入"),
        VERIFY_CODE_INCORRECT(2, "验证码不正确"),
        ACCOUNT_NON_EXIST(3, "账号不存在"),
        USCC_ACCOUNT_NON_EXIST(4, "未查出账号，请检查统一社会认证编码是否输入正确"),
        LICENSE_CODE_INCORRECT(5, "授权码错误或已失效，请检查是否输入正确");

        int code;
        String msg;

        ErrType(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

}
