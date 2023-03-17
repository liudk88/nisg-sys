package com.hcxinan.sys.cache.inte;

import com.hcxinan.core.inte.system.ISysRule;

/**
 * @Title:
 * @Author: Fly
 * @Date: 2021/7/23 - 10:51
 * @Description:
 */
public interface ICacheCode extends ISysRule {

    void setCode(String code);

    String getCode();

    void setSubcode(String subcode);

    String getSubcode();

    void setCname(String cname);

    String getCname();

    void setValid(Integer valid);

    Boolean getValid();

    void setSeq(Integer seq);

    Integer getSeq();

    void setParams1(String params1);

    String getParams1();

    void setParams2(String params2);

    String getParams2();

    void setDes(String des);

    String getDes();
}
