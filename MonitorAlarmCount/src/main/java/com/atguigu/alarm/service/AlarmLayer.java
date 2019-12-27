package com.atguigu.alarm.service;

import com.atguigu.alarm.entity.Alarm;
import com.atguigu.alarm.entity.Rule;
import com.atguigu.alarm.util.MysqlUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * 报警层：根据统计层的情况结合报警规则，对达到阈值的规则进行报警，
 * 存入数据库中表格alarms
 */
public class AlarmLayer {
    private static Logger log = Logger.getLogger(AlarmLayer.class);
    private CountLayer countLayer;

    public AlarmLayer(){
    }
    public AlarmLayer(CountLayer countLayer){
        this.countLayer = countLayer;
        log.warn("AlarmLayer init done!");
    }

    /**
     * 根据统计层和规则层进行报警
     */
    public void alarm(){
        for (Map.Entry<Integer, Map<Integer, Map<String, Integer>>> grwc : countLayer.gameRuleWordCount.entrySet()) {
            Integer game_id = grwc.getKey();
            for (Map.Entry<Integer, Map<String, Integer>> rwc : grwc.getValue().entrySet()) {
                Integer rule_id = rwc.getKey();
                Rule rule = countLayer.idRule.get(rule_id);

                //报警算法
                double sum = 0,count =0;
                for (Map.Entry<String, Integer> wc : rwc.getValue().entrySet()) {
                    sum += wc.getValue();
                    count += 1;
                }
                //0按词取平均值，1按词之和
                if(rule.getType() == 0){
                    sum /=count;
                }
                if(sum >= rule.getThreshold()){
                    //超过词频规则，发送报警信息
                    /**
                     *  private int game_id;
                     *     private String game_name;
                     *     private String words;
                     *     private String words_freq;
                     *     private int rule_id;
                     *     private String rule_name;
                     *     private int has_sent;    //是否已经发送，默认值为0
                     *     private int is_problem;  //是否真有问题，默认-1：未确认；0：不是；1：是
                     */
                    Alarm alarm = new Alarm();
                    alarm.setGame_id(game_id);
                    alarm.setGame_name(rule.getGame_name());
                    alarm.setWords(rule.getWords());
                    alarm.setWords_freq(map2Str(rwc.getValue()));
                    alarm.setRule_id(rule_id);
                    alarm.setRule_name(rule.getRule_name());
                    alarm.setHas_sent(0);
                    alarm.setIs_problem(-1);
                    //将报警信息插入数据库中alarms表
                    MysqlUtils.insert("alarms",alarm);
                    log.warn(alarm.toString());
                    //更新词频统计数据到0
                    for (String w : rwc.getValue().keySet()) {
                        rwc.getValue().put(w,0);
                    }
                }
            }
        }
    }

    /**
     * 将map集合按照key:value形式转换为字符串
     */
    public String map2Str(Map<String,Integer> map){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sb.append(String.format("%s:%d",entry.getKey(),entry.getValue()));
        }
        return sb.toString();
    }

}
