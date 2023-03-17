package com.hcxinan.sys.cache.inte;

/**
 * @Title: (对实现系统缓存加载接口的数据,如果在 Spring 当中进行配置后系统会默认调用接口)
 * @Author: Fly
 * @Date: 2021/7/23 - 10:48
 * @Description:
 */
public interface ISystemCache {
    /*起动时进行加载*/
    void  load();

    /*重新进行加载*/
    void reload() ;

    void  release();

    void remove(String cacheCode);

    void reload(String cacheCode);

    /*系统加载功能的用途描述*/
    String getDescription();
}
