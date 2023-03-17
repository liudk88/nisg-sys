package com.hcxinan.sys.service;

import com.alibaba.fastjson.JSONArray;
import com.hcxinan.sys.mapper.BaseDaoSupport;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Author: Fly
 * @Date: 2021/7/23 - 10:56
 * @Description:
 */
@Service("commonService")
public class CommonService implements ICommonService {
//    private static final Logger logger = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    protected BaseDaoSupport dao;

    public BaseDaoSupport getDao() {
        return dao;
    }

  /*  @Autowired
    @Qualifier(value = "dao")*/
    public void setDao(BaseDaoSupport dao) {
        this.dao = dao;
    }

    public int delete(String statement) {
        return this.dao.getSqlSession().delete(statement);
    }

    public int delete(String statement, Object parameter) {
        return this.dao.getSqlSession().delete(statement, parameter);
    }

    public int update(String statement) {
        return this.dao.getSqlSession().update(statement);
    }

    public int update(String statement, Object parameter) {
        return this.dao.getSqlSession().update(statement, parameter);
    }

    public int insert(String statement) {
        return this.dao.getSqlSession().insert(statement);
    }

    public int insert(String statement, Object parameter) {
        return this.dao.getSqlSession().insert(statement, parameter);
    }

    /**
     * 批量插入
     *
     * @param list     要更新的数据列表
     * @param mapperId 对应执行MYBATIS配置的SQLID 全路径 = namespace + id
     * @return
     * @throws SQLException
     */
    public int[] batchInsert(List list, String mapperId) {
        return batchActionOper(list, mapperId);
    }

    /**
     * 批量更新
     *
     * @param list     要更新的数据列表
     * @param mapperId 对应执行MYBATIS配置的SQLID 全路径 = namespace + id
     * @return
     * @throws SQLException
     */
    public int[] batchUpdate(List list, String mapperId) {
        return batchActionOper(list, mapperId);
    }

    /**
     * 批量删除
     *
     * @param list     要更新的数据列表
     * @param mapperId 对应执行MYBATIS配置的SQLID 全路径 = namespace + id
     * @return
     * @throws SQLException
     */
    public int[] batchDelete(List list, String mapperId) {
        return batchActionOper(list, mapperId);
    }

    protected int[] batchActionOper(List list, String mapperId) {
        if (list == null || list.isEmpty()) {
            throw new NullPointerException("参数错误，需要操作的列表不能为空");
        }

        if (mapperId == null || "".equals(mapperId.trim())) {
            throw new NullPointerException("参数错误，需要执行的mapperId 不能为空 ");
        }
//		SqlSessionTemplate st = (SqlSessionTemplate)this.dao.getSqlSession();
//		Connection conn = SqlSessionUtils.getSqlSession(
//		                st.getSqlSessionFactory(), st.getExecutorType(),
//		                st.getPersistenceExceptionTranslator()).getConnection();
        Configuration c = this.dao.getSqlSession().getConfiguration();
        ManagedTransactionFactory managedTransactionFactory = new ManagedTransactionFactory();
        BatchExecutor batchExecutor = new BatchExecutor(c, managedTransactionFactory.newTransaction(this.dao.getSqlSession().getConnection()));
        MappedStatement mStmt = c.getMappedStatement(mapperId);
        if (mStmt == null) {
            throw new NullPointerException("参数错误，无法通过[" + mapperId + "]找到对应的配置MappedStatement");
        }
        try {
            for (Object obj : list) {
                batchExecutor.doUpdate(mStmt, obj);
            }
            batchExecutor.flushStatements();
        } catch (Exception e) {
            /*
            * TODO:
            * 之前把荔怡把日志这块依赖到javaweb-common里，需要去掉，但日志依赖有点问题，赶项目，后面解决
            * @liudk 21-9-16
            * */
            System.out.println("批量操作失败：数据如下");
            try {
                System.out.println(JSONArray.toJSONString(list));
//                logger.error(JSONArray.toJSONString(list));
            } catch (Exception e1) {
                try {
                    System.out.println(BeanUtils.describe(list).toString());
//                    logger.error(BeanUtils.describe(list).toString());
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                }
            }
            e.printStackTrace();
            System.out.println("批量操作失败");
//            logger.error("批量操作失败",e);
        }
        return null;
    }


    public List queryList(String selectId, Object param) {
        return dao.getSqlSession().selectList(selectId, param);
    }

    public Map queryMap(String selectId, Object param) {
        return (Map) dao.getSqlSession().selectOne(selectId, param);
    }

    public Object queryObject(String selectId, Object param) {
        return dao.getSqlSession().selectOne(selectId, param);
    }

}
