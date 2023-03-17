package com.hcxinan.sys.constant;

import com.hcxinan.sys.inte.ICodeUtil;
import com.hcxinan.sys.util.code.RandomCodeUtil;
import com.hcxinan.sys.util.code.RegisterCodeUtil;
import com.hcxinan.sys.util.code.Sm4CodeUtil;
import org.apache.commons.lang3.StringUtils;

public enum CodeType {

    CHECK(Sm4CodeUtil.class, "系统校验码工具类", "CHECK"),
    //    SMS,
//    MAIL,
    RANDOM(RandomCodeUtil.class, "随机验证码工具类", "RANDOM"),
    REGISTER(RegisterCodeUtil.class, "离线端管理工具类", "REGISTER");

    private Class<? extends ICodeUtil> codeClass; //对应的工具类
    private String codeName; //备注名
    private String unique; //唯一标识

    private CodeType(Class<? extends ICodeUtil> codeClass, String codeName, String unique) {
        this.codeClass = codeClass;
        this.codeName = codeName;
        this.unique = unique;
    }

    /**
     * @param unique 验证码唯一标识
     * @return CodeType
     */
    public static CodeType getInstance(String unique) {
        if (StringUtils.isBlank(unique)) {
            return null;
        } else {
            unique = unique.toUpperCase();
        }
        for (CodeType code : CodeType.values()) {
            if (code.unique.equalsIgnoreCase(unique)) {
                return code;
            }
        }
        return null;
    }

    public Class<? extends ICodeUtil> getCodeClass() {
        return codeClass;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getUnique() {
        return unique;
    }

}
