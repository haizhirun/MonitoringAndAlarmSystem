package com.atguigu.alarm.dao;

import com.atguigu.alarm.entity.MonitorGames;
import com.atguigu.alarm.util.MysqlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MonitorGamesDao {
    /**
     * 获取所有监视游戏
     *
     * @return
     */
    public static List<MonitorGames> getMonitorGames() {
        return MysqlUtils.queryByBeanListHandler("select * from monitor_games", MonitorGames.class);
    }

    /**
     * 以[game_id -> monitorGame]的形式获取所有监视游戏
     * @return
     */
    public static Map<Integer,MonitorGames> getMapMonitorGames(){
        Map<Integer,MonitorGames> mem = new HashMap<>();
        for(MonitorGames game : getMonitorGames()){
            mem.put(game.getGame_id(),game);
        }
        return mem;
    }
}
