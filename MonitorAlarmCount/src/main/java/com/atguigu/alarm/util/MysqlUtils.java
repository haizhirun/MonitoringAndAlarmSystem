package com.atguigu.alarm.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 利用common-DBTtils提供数据库查询服务
 */
public class MysqlUtils {
    private static Logger log = Logger.getLogger(MysqlUtils.class);
    private static Connection conn = null;
    private static ComboPooledDataSource cpds = new ComboPooledDataSource();

    static {
        try {
            cpds.setDriverClass("com.mysql.jdbc.Driver");
            cpds.setJdbcUrl(ConfigUtils.getConfig("url"));
            cpds.setUser(ConfigUtils.getConfig("username"));
            cpds.setPassword(ConfigUtils.getConfig("password"));
            cpds.setMinPoolSize(5);
            cpds.setAcquireIncrement(5);
            cpds.setMaxPoolSize(20);
            cpds.setMaxStatements(180);
            // 检测连接配置
            cpds.setPreferredTestQuery("SELECT 1");
            cpds.setIdleConnectionTestPeriod(10000);
            // 获取到连接时就同步检测
            cpds.setTestConnectionOnCheckin(true);
        }catch (PropertyVetoException e){
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 获取连接
     * @return
     */
    public static Connection getConnection(){
        try {
            conn = cpds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 将对象插入到指定表中
     * @param table 数据库中表名
     * @param obj 对象
     */
    public static void insert (String table,Object obj){
        try {
            QueryRunner qr = new QueryRunner();
            String sql = String.format("insert into $s (%s) values (%s);",table, StringUtils.join(getFieldName(obj),","),
                    StringUtils.join(getFieldValues(obj),","));
            qr.update(getConnection(),sql);

        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }finally {
            destory();
        }
    }

    /**
     * 根据sql搜索语句返回对应对象的集合
     * @param sql 查询sql语句
     * @param beanType 返回对象的类型
     * @param <T>
     * @return
     */
    public static <T> List<T> queryByBeanListHandler(String sql,Class<T> beanType){
        List<T> rs = null;
        try {
            QueryRunner qr = new QueryRunner();
            rs = qr.query(getConnection(), sql, new BeanListHandler<T>(beanType));
        }catch (Exception e){
            log.error(ExceptionUtils.getStackTrace(e));
        }finally {
            destory();
        }
        return rs;
    }


    /**
     * 获取属性名数组
     * @param obj
     * @return
     */
    private static List<String> getFieldName(Object obj){
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for(int i = 0; i < fields.length; i++){
            if(fields[i].getName().equals("id")){
                continue; //id一般为自增
            }
            fieldNames.add(fields[i].getName());
        }
        return fieldNames;
    }

    /**
     * 获取对象的所有属性值
     * @param obj
     * @return
     */
    private static List<String> getFieldValues(Object obj){
        List<String> fieldValues = new ArrayList<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for(int i = 0; i < fields.length; i++){
            if(fields[i].getName().equals("id")){
                continue; //id一般为自增
            }
            String value = getFieldValueByName(fields[i].getName(),obj);
            String type = fields[i].getType().toString();
            //判断值的类型，如果是字符串，需要添加''用于sql处理
            if("class java.lang.String".equals(type)){
                value = "'" + value + "'";
            }
            fieldValues.add(value);
        }
        return fieldValues;
    }

    /**
     * 根据对象的属性名获取对应的属性值
     * @param name
     * @param obj
     * @return
     */
    private static String getFieldValueByName(String name, Object obj) {
        try {
            //构造getValue形式
            String getter = "get" + name.substring(0,1).toUpperCase() + name.substring(1);
            System.out.println(getter);
            Method method = obj.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(obj, new Object[]{});
            return value.toString();
        }catch (Exception e){
            return null;
        }
    }

    private static void destory() {
        DbUtils.closeQuietly(conn);
    }

    /**
     * 获取指定table的更新时间
     * @return 单位为秒
     */
    public static Long getUpdateTime(String table) {
        //获取table表上次更新时间sql语句
        /**
         * +------------+---------------------+
         * | TABLE_NAME | UPDATE_TIME         |
         * +------------+---------------------+
         * | rules      | 2019-11-25 11:10:12 |
         * +------------+---------------------+
         */
        String sql = String.format("SELECT TABLE_NAME,UPDATE_TIME FROM information_schema.TABLES WHERE information_schema.TABLES.TABLE_NAME = '%s';",table);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            String dateStr = queryByMapHandler(sql).get("UPDATE_TIME").toString();
            System.out.println(dateStr);
            return sdf.parse(dateStr).getTime()/1000;  //单位为秒
        }catch (Exception e){
            log.error(ExceptionUtils.getStackTrace(e));
            return 0L;
        }
    }

    /**
     * 将sql查询结果中的第一行转换为键值对，键为列名
     */
    public static Map<String,Object> queryByMapHandler(String sql){
        Map<String,Object> rs = null;
        try {
            QueryRunner qr = new QueryRunner();
            rs = qr.query(getConnection(),sql,new MapHandler());
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }finally {
            destory();
        }
        return rs;
    }

    public static void main(String[] args) {
        System.out.println(getUpdateTime("rules"));
    }
}
