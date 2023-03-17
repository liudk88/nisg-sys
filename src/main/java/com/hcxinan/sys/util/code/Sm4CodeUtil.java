package com.hcxinan.sys.util.code;

import com.hcxinan.core.inte.system.ISysRule;
import com.hcxinan.core.inte.system.ISysRuleMs;
import com.hcxinan.sys.inte.ICodeUtil;
import com.hcxinan.sys.util.SM4Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 *
 */
@Component
public class Sm4CodeUtil implements ICodeUtil {

    private static final Logger log = LoggerFactory.getLogger(Sm4CodeUtil.class);

    private static ISysRuleMs ruleMs;

    //sm4默认密钥，值从字典读取，字典未配置则使用此默认值
    private String hexKey = "4fa8f4760be13dc935628ec6e8cc6bb7";

    //校验码取前几位数
    private int firstFewDigits = 6;

    //日期转换类
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    //sm4密钥父键
    private static final String HEX_KEY_CODE = "SYSCONFIG";

    //sm4密钥子键
    private static final String HEX_KEY_SUB_CODE = "SM4_HEX_SECRET_KEY";

    /**
     * Default constructor
     */
    public Sm4CodeUtil() {
        this.hexKey = getHexKey();
    }

    /**
     * 校验验证码
     * @param code
     * @param obj
     */
    public boolean verify(String code, Object obj) {
        if (obj != null && StringUtils.isNotBlank(code)) {
            return code.equals(getCode(obj));
        }
        return false;
    }

    /**
     * 根据账号与当天日期生成系统校验码
     * @param obj
     */
    public String getCode(Object obj) {
        Objects.requireNonNull(obj, "账号为空，系统校验码无法生成");
        String sourceCode = obj + dateFormat.format(System.currentTimeMillis());
        String encryptStr = SM4Util.encryptEcb(hexKey, sourceCode);
        if (!sourceCode.equals(encryptStr) && encryptStr.length() >= 6) {
            return encryptStr.substring(0, firstFewDigits);
        } else {
            return null;
        }
    }

    /**
     * @return sm4密钥
     */
    String getHexKey() {
        try {
            ISysRule rule = ruleMs.getRule(HEX_KEY_CODE, HEX_KEY_SUB_CODE);
            if (rule != null) return rule.getVal();
        } catch (NullPointerException e) {
            log.warn("字典未配置sm4密钥，使用系统默认密钥");
        }
        return hexKey;
    }

    @Autowired
    public void setRuleMs(ISysRuleMs ruleMs) {
        this.ruleMs = ruleMs;
    }
}