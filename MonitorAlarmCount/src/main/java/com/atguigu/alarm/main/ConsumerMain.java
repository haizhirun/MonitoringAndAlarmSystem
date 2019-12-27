package com.atguigu.alarm.main;

import com.atguigu.alarm.entity.Record;
import com.atguigu.alarm.service.AlarmLayer;
import com.atguigu.alarm.service.CountLayer;
import com.atguigu.alarm.service.FilterLayer;
import com.atguigu.alarm.util.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

/**
 *  通过一个while(true)程序，反复从kafka中拉取数据，进行过滤，统计报警
 *  对报警过的规则，需要在统计曾归零
 */
public class ConsumerMain {
    private static Logger log = Logger.getLogger(ConsumerMain.class);
    private static Gson gson = new Gson();
    private Long nextReloadTime;
    private Long lastUpdateTime = 0L; //规则表上次更新时间
    private Long lastGamesUpdateTime = 0L; //监控游戏上次更新时间
    private Long kafkaLogTimes = 0L;

    //过滤层
    private FilterLayer filterLayer;
    //统计层
    private CountLayer countLayer;
    //报警层
    private AlarmLayer alarmLayer;

    public ConsumerMain(long beginTime){
        filterLayer = new FilterLayer();
        countLayer = new CountLayer();
        alarmLayer = new AlarmLayer(countLayer);
        long currentTime = TimeUtils.currentTimeSeconds();
        nextReloadTime = currentTime + ConfigUtils.getIntValue("reload_interval");

        lastUpdateTime = MysqlUtils.getUpdateTime("rules");
        lastGamesUpdateTime = MysqlUtils.getUpdateTime("monitor_games");
    }

    public void run(){
        KafkaUtils kafkaUtils = KafkaUtils.getInstance();
        if(!kafkaUtils.initialize()){
           //kafka初始化失败
           log.error("kafka init error!");
           System.exit(-1);
        }
        KafkaConsumer<String, String> consumer = kafkaUtils.getConsumer();

        long count = 0;//记录消息条数
        //通过while true循环，反复从kafka中拉去数据记录，进行过滤统计
        while(true){
            try{
                ConsumerRecords<String, String> records = consumer.poll(200);
                for (ConsumerRecord<String, String> record : records) {
                    //当消息数量到达一定数量时，打印一条日志消息
                    if(count++ % 10000 == 0){
                        log.warn("[CurDataCount] count: " + count);
                    }
                    //解析JSON数据
                    Record r = gson.fromJson(record.value(), Record.class);
                    if(filterLayer.filter(r)){
                        continue;
                    }
                    countLayer.addRecord(r);
                }
                alarmLayer.alarm();

                if(kafkaLogTimes++ % 10 == 0){
                    kafkaUtils.tryCommit(records,true);
                }else{
                    kafkaUtils.tryCommit(records,false);
                }

                //重新加载
                if(nextReloadTime <= TimeUtils.currentTimeSeconds()){
                    long updateTime = MysqlUtils.getUpdateTime("rules");
                    long gamesUpdateTime = MysqlUtils.getUpdateTime("monitor_games");
                    if(updateTime!=lastUpdateTime || gamesUpdateTime != lastGamesUpdateTime){
                        log.warn("rules or games changed!");
                        countLayer.reload();
                        lastUpdateTime = updateTime;
                        lastGamesUpdateTime = gamesUpdateTime;
                    }

                    if(CommonUtils.isFileChange("patterns_appstore.txt","patterns_forum.txt")){
                        TrashFilterUtils.reload();
                    }
                    //获取下一次加载时间
                    while(nextReloadTime <= TimeUtils.currentTimeSeconds()){
                        nextReloadTime += ConfigUtils.getIntValue("reload_interval");
                    }
                }
            }catch (Exception e){
                log.error("main error " + ExceptionUtils.getMessage(e));
            }
        }
    }

    public static void main(String[] args) {
        final ConsumerMain consumerMain = new ConsumerMain(TimeUtils.currentTimeSeconds());
        log.warn("All things init done!");
        consumerMain.run();
    }

}
