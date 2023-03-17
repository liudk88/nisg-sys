package com.hcxinan.sys;

import com.hcxinan.core.util.DbTableUtil;
import com.morph.db.ITable;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class H2DataSource implements DataSource {

    private DataSource source = null;

    private static boolean hasInit=false;

    public H2DataSource() {
//        System.out.println("============H2DataSource数据库初始化"+hasInit);
        EmbeddedDatabaseBuilder embeddedDatabaseBuilder=new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2);
        if(hasInit==false){
            hasInit=true;

            /*把表结构json文件进行解析，建表语句生成到schema.sql下*/
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            try {
                Resource[] resources = resolver.getResources("classpath:initDb/schema/*.json");
                List<String> jsonstrs=new ArrayList<>();

                for(Resource resource:resources){
//                    System.out.println("=============---"+resource.toString());
                    jsonstrs.add(process(resource.getInputStream()));
                }
                if (jsonstrs.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (String jsonstr : jsonstrs) {
                        ITable table = DbTableUtil.getMotable(jsonstr,false);
                        String createTableSql = table.getSql(true);
//                        System.out.println(createTableSql);
//                        String createTableSql = DbTableUtil.getTableSchemaSql(table, false, false);
                        sb.append(createTableSql + ";\n");
                    }
                    /*System.out.println("============");
                    System.out.println(sb.toString());*/
                    String filePath=this.getClass().getResource("/schema.sql").getFile();
                    File schemaFile= new File(filePath);
                    if(sb.length()>0 && schemaFile!=null && schemaFile.exists()){
                        FileUtils.write(schemaFile,sb.toString());
                    }
                }
                embeddedDatabaseBuilder
                        .addScripts("classpath:schema.sql")
//                .addScripts("classpath:otherSchema.sql")
                        .addScripts("classpath:data.sql");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*this.source = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScripts("classpath:schema.sql")
//                .addScripts("classpath:otherSchema.sql")
                .addScripts("classpath:data.sql")
                .build();*/
        this.source=embeddedDatabaseBuilder.build();
    }

    private String process(InputStream input) throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader(input));
        String line=null;
        StringBuilder sb=new StringBuilder();

        while((line = br.readLine())!=null && line.length() !=0) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return source.getConnection(username,password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return source.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return source.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return source.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        source.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        source.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return source.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return source.getParentLogger();
    }
}
