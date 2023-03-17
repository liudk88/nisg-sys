package com.hcxinan.sys.inte;

/**
 * 系统校验校验工具
 */
public interface ICodeUtil {

    /**
     * 校验验证码
     * @param code 
     * @param obj
     */
    boolean verify(String code, Object obj);

    /**
     * 获取验证码
     * @param obj
     */
    String getCode(Object obj);

}