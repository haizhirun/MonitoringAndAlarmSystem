package com.atguigu.alarm.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 关于配置文件的工具类
 */
public class ConfigUtils {
    public static final String resourceFullName = "/config.properties";
    public static final Properties props = new Properties();

    static {
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(ConfigUtils.class.getResourceAsStream(resourceFullName));
            props.load(is);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据config.properties中key获取value
     * @param key
     * @return
     */
    public static String getConfig(String key){
        return props.getProperty(key);
    }

    /**
     * @param key
     * @return
     */
    public static boolean getBooleanValue(String key){
        if("true".equals(props.get(key))){
            return true;
        }else{
            return false;
        }
    }

    public static Integer getIntValue(String key){
        return Integer.parseInt(getConfig(key));
    }

}
