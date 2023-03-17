package com.hcxinan.sys.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

public class NisgPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
            throws BeansException {
        try {
            String dburl=props.getProperty("app.jdbc.url");
            String dbip=props.getProperty("app.jdbc.ip");
            String dbport=props.getProperty("app.jdbc.port");

            //加密解密处理
            String encrypt=null;//判断是否需要加密，只有等于0的时候不加密，其他情况加密
            if(props.getProperty("app.jdbc.encrypt")!=null){
//                encrypt=PlatUtil.decrypt(props.getProperty("app.jdbc.encrypt"));
            }

            /*替换ip和端口:
            * 考虑到未来可能使用不同的数据库，那么对于不同的数据库，ip、端口、用户名、密码这些都是统一的，而url的格式确是
            * 不同的，所以在jdbc.properties中只定义这些通用信息，而把url放到maven中配置，这样只要升级pom.xml就可以
            * 更改不同的数据库。（不需要重新写jdbc.properties）
            * */
            if(StringUtils.isNotBlank(dburl)){
                dburl=dburl.replace("dbip",dbip);
                dburl=dburl.replace("dbport",dbport);
                props.setProperty("app.jdbc.url", dburl);
            }
            super.processProperties(beanFactoryToProcess, props);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BeanInitializationException(e.getMessage());
        }
    }
}
