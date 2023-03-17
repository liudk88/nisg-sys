package com.hcxinan.sys.util.code;

import com.hcxinan.sys.cache.CacheManager;
import com.hcxinan.sys.inte.ICodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 系统随机验证码工具
 */
public class RandomCodeUtil implements ICodeUtil {

    private static final Logger log = LoggerFactory.getLogger(RandomCodeUtil.class);

    private final int codeSize = 4; //验证码默认大小，4位数

    private boolean caseSensitive = false; //比较验证码时候是否区分大小写，默认不区分

    private Random random = new Random();

    /**
     * Default constructor
     */
    public RandomCodeUtil() {
        try {
            if (CacheManager.getInstance().getCodes("RANDOM_CODE").size() == 0) {
                log.info("字典未配置校验码比较是否区分大小写，默认不区分");
            } else {
                String bool = CacheManager.getInstance().getCode("RANDOM_CODE", "CASE_SENSITIVE").getCname();
                if (bool != null) {
                    this.caseSensitive = Boolean.valueOf(bool);
                }
            }
        } catch (NullPointerException e) {
            log.info("字典未配置校验码比较是否区分大小写，默认不区分");
        }
    }

    /**
     * 校验验证码
     * @param code 
     * @param obj
     */
    public boolean verify(String code, Object obj) {
        if (this.caseSensitive) {
            return code.equals(obj);
        } else {
            return code.equalsIgnoreCase((String) obj);
        }
    }

    /**
     * 生成验证码
     * @param obj
     */
    public String getCode(Object obj) {
        int len = this.codeSize;
        if (obj != null) {
            len = Integer.parseInt(obj.toString());
        }
        return createCode(len);
    }

    /**
     * 生成随机字符串，内容范围包括大写字母、小写字母、正整数
     * @param len
     * @return
     */
    String createCode(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            //字符类型：0-数字，1-小写英文，2-大写英文
            int type = random.nextInt(3);
            switch (type) {
                case 0:
                    sb.append(createDigit());
                    break;
                case 1:
                    sb.append(createLowerCase());
                    break;
                case 2:
                    sb.append(createUpperCase());
                    break;
            }
        }
        return sb.toString();
    }


    /**
     * 随机生成一个大写英文字母
     * @return 大写字符
     */
    char createUpperCase() {
        //A:65 -- Z:65+25
        return (char) (random.nextInt(26) + 65);
    }

    /**
     * 随机生成一个小写英文字母
     * @return 小写字符
     */
    char createLowerCase() {
        //a:97 -- z:97+25
        return (char) (random.nextInt(26) + 97);
    }

    /**
     * 随机生成一个正整数字符
     * @return 正整数字符
     */
    char createDigit() {
        //0 -- 9
        return (char) (random.nextInt(10) + 48);
    }

}