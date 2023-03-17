package com.hcxinan.sys.util;

import com.hcxinan.sys.constant.CodeType;
import com.hcxinan.sys.inte.ICodeUtil;

/**
 *
 */
public class CodeFactory {

    /**
     * @param codeType
     * @return
     */
    public static ICodeUtil getCodeUtil(CodeType codeType) throws Exception {
        return codeType.getCodeClass().newInstance();
    }

}