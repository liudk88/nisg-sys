package com.hcxinan.sys.util;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
*@Description 平台工具类
*@Param 
*@Return 
*@Author liudk
*@DateTime 20-10-9 下午3:24
*/
@Component("platUtil")
public class PlatUtil implements ApplicationContextAware {
    private static final Logger logger = Logger.getLogger(PlatUtil.class);

    private static ApplicationContext ctx;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public static Object getBean(String beanName) {
        return ctx.getBean(beanName);
    }

    public static synchronized String getId() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Long.toString(System.nanoTime(), 36).toUpperCase();
    }
}
