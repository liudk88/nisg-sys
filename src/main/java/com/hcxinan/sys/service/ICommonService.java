package com.hcxinan.sys.service;

import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Author: Fly
 * @Date: 2021/7/23 - 10:58
 * @Description:
 */
public interface ICommonService {

    List queryList(String selectId, Object param);

    Object queryObject(String selectId,Object param);

    Map queryMap(String selectId, Object param);

}
