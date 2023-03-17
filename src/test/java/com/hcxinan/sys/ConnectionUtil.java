package com.hcxinan.sys;

import com.morph.db.IConnectionUtil;
import com.morph.util.TableCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;

@Component("connectionUtil")
public class ConnectionUtil implements IConnectionUtil {
    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init(){
        TableCache.setConUtil(this);
    }

    @Override
    public Connection openConnection() {
        return DataSourceUtils.getConnection(dataSource);//带有事务处理的连接
    }

    @Override
    public void doReleaseConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection,dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
