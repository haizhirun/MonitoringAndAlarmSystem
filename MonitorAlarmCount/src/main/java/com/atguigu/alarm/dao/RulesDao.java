package com.atguigu.alarm.dao;

import com.atguigu.alarm.entity.Rule;
import com.atguigu.alarm.util.MysqlUtils;

import java.util.List;

public class RulesDao {

    public static List<Rule> getGameRules(){
        return MysqlUtils.queryByBeanListHandler("select * from rules where state=0;",Rule.class);
    }

}
