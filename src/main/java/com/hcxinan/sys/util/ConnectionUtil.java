package com.hcxinan.sys.util;

import com.morph.db.DbType;
import com.morph.db.IConnectionUtil;
import com.morph.util.TableCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@Component("connectionUtil")
public class ConnectionUtil implements IConnectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionUtil.class);
    @Autowired
    private DataSource dataSource;

    private Map<Integer,Integer> countConnectionUseTimeMap=new HashMap<>();//统计连接使用的时间
    private Map<Integer,Connection> connectionMap=new HashMap<>();//记录连接

    private Timer timer = new Timer();

    private int userWarnLimit=25;//超过多少秒后提示
    private int clearLimit=100;//超过多少秒后释放

    @Value("${morph.dbType}")
    private DbType dbType;

    @PostConstruct
    public void init(){
        if(dbType!=null){
            TableCache.init(this,dbType);
        }else{
            TableCache.setConUtil(this);
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(countConnectionUseTimeMap.size()>0){
                    Set<Integer> sets=countConnectionUseTimeMap.keySet();
                    for(Integer key:sets){
                        int useTime=countConnectionUseTimeMap.get(key);
                        if(useTime>userWarnLimit){
                            logger.error("连接"+key+"已经超过了"+userWarnLimit+"s使用而未释放！");
                        }
                        if(useTime>clearLimit){
                            Connection connection=connectionMap.get(key);
                            doReleaseConnection(connection);
                        }else{
                            countConnectionUseTimeMap.put(key,countConnectionUseTimeMap.get(key)+1);//每隔一秒加一
                        }
                    }
                }
            }
        },0,5L* 1000);
    }

    @Override
    public Connection openConnection() {
        Connection connection=DataSourceUtils.getConnection(dataSource);
//        logger.info("获取数据库连接 -> "+connection.hashCode());
        countConnectionUseTimeMap.put(connection.hashCode(),0);
        connectionMap.put(connection.hashCode(),connection);
        return connection;//带有事务处理的连接
    }

    @Override
    public void doReleaseConnection(Connection connection) {
//        logger.info("释放数据库连接 <- "+connection.hashCode());
        countConnectionUseTimeMap.remove(connection.hashCode());
        connectionMap.remove(connection.hashCode());
        DataSourceUtils.releaseConnection(connection,dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
