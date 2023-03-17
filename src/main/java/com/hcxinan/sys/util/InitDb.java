package com.hcxinan.sys.util;

import com.hcxinan.core.inte.system.ISysConfig;
import com.hcxinan.core.util.DbTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@Component("initDb")
@DependsOn("connectionUtil")
public class InitDb {
    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired(required = false)
    private ISysConfig sysConfig;

    @PostConstruct
    @Transactional
    public void initDb() throws IOException, SQLException {
        if(sysConfig==null || sysConfig.enableAutoUpdateDb()){{
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
            TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态
            try {
                Connection connection=transactionManager.getDataSource().getConnection();
                Resource[] resources = resolver.getResources("classpath*:initDb/schema/*.json");
                /*for(Resource resource:resources){
                    System.out.println(resource.getFilename());
                }*/
                DbTableUtil dbTableUtil=new DbTableUtil();
                dbTableUtil.syncTables(connection,resources,true);
                transactionManager.commit(status);
            } catch (Exception e) {
                e.printStackTrace();
                transactionManager.rollback(status);
            }
        }}
    }
}
