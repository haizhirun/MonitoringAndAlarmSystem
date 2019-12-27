package com.atguigu.alarm.util;

import java.io.File;
import java.net.URL;

/**
 * 定义一些公共工具函数
 */
public class CommonUtils {
    //保存文件上次更新时间
    private static long lastFileUpdateTime = 0L;

    /**
     * 闯入多个文件名,任何一个文件有变动则返回true
     * @return
     */
    public static boolean isFileChange(String ... files){
        boolean flag = false;
        for (String file : files) {
            URL res = CommonUtils.class.getResource("/" + file);
            if(res == null){
                continue;
            }
            long updateTime = new File(res.getFile()).lastModified();
            if(updateTime != lastFileUpdateTime){
                flag = true;
                lastFileUpdateTime = updateTime;
            }
        }
        return flag;
    }
}
