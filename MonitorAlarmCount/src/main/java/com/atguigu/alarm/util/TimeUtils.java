package com.atguigu.alarm.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class TimeUtils {
    /**
     * 将指定时间戳转换为yyyy-MM--dd HH:mm:ss
     * @param timeSec 传入参数单位为秒
     * @return
     */
    public static String time2String(Long timeSec){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM--dd HH:mm:ss");
        return sdf.format(new Date(timeSec*1000));
    }

    /**
     * 获取指定时间戳（s）的那天开始时间00:00:00对应的时间戳
     * @param timeSec
     * @return
     */
    public static Long getStartOfTime(Long timeSec){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeSec*1000);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal.getTimeInMillis()/1000;
    }

    /**
     * 获取当前时间戳
     * @return 单位为秒
     */
    public static long currentTimeSeconds(){
        return System.currentTimeMillis()/1000;
    }
}
