package com.atguigu.alarm.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class KafkaUtils {
    static Logger log = Logger.getLogger(KafkaUtils.class);
    private static KafkaUtils instance = new KafkaUtils();
    private KafkaProducer<String,byte[]> producer = null;
    private KafkaConsumer<String,String> consumer = null;
    private String producerTopic = "";
    private boolean autoCommit = false;
    private Set<TopicPartition> assignedPartitions = null;

    private KafkaUtils(){}

    public static KafkaUtils getInstance(){
        return instance;
    }
    public KafkaProducer<String, byte[]> getProducer() {
        return producer;
    }

    public KafkaConsumer<String, String> getConsumer() {
        return consumer;
    }

    public String getProducerTopic() {
        return producerTopic;
    }

    public boolean initialize(){
        try {
            Properties consumerProps = new Properties();

            // kafka original conf
            consumerProps.put("bootstrap.servers", ConfigUtils.getConfig("bootstrap.servers"));

            // consumer special
            consumerProps.put("group.id", ConfigUtils.getConfig("group.id"));
            consumerProps.put("enable.auto.commit", autoCommit);
            consumerProps.put("auto.offset.reset", ConfigUtils.getConfig("auto.offset.reset"));
            consumerProps.put("session.timeout.ms", "35000");
            consumerProps.put("max.partition.fetch.bytes", 64 * 1024 * 1024); // 64MB
            consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            consumerProps.put("max.poll.records", ConfigUtils.getConfig("max.poll.records"));

            consumer = new KafkaConsumer<>(consumerProps);
            //当手动给分区赋值时，subscribe函数不应该使用
            String topic = ConfigUtils.getConfig("consumer.topic");
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            LinkedList<TopicPartition> topicPartitions = new LinkedList<>();

            for (PartitionInfo info : partitionInfos) {
                log.info("topic has partition: " + info.partition());
                TopicPartition topicAndPartition = new TopicPartition(topic, info.partition());
                topicPartitions.add(topicAndPartition);
            }
            consumer.assign(topicPartitions);

            assignedPartitions = consumer.assignment();
            log.info("Initial partition positions:");
            logPosition(false);

            if(ConfigUtils.getBooleanValue("frombegin")){
                consumer.seekToBeginning(topicPartitions);
                log.info("Seek to beginning is set, after seek to beginning, now partition positions:");
                logPosition(false);
            }

            if(ConfigUtils.getBooleanValue("fromend")){
                consumer.seekToEnd(topicPartitions);
                log.info("Seek to end is set, after seek to end, now partition positions:");
                logPosition(false);
            }
        }catch (Exception e){
            log.error("initial KafkaUtils fails, e: " + ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    private void logPosition(boolean debug) {
        for (TopicPartition assignedPartition : assignedPartitions) {
            if (debug) {
                log.debug(String.format("partition %d position: %d", assignedPartition.partition(),
                        consumer.position(assignedPartition)));
            } else {
                log.info(String.format("partition %d position: %d", assignedPartition.partition(),
                        consumer.position(assignedPartition)));
            }
        }
    }

    public void tryCommit(ConsumerRecords<String,String> fetchedRecords,boolean needLog){
        if(fetchedRecords.count()>0 && !autoCommit){
            consumer.commitSync();
            if(needLog){
                log.info("=== After commitSync, now partition positions:");
                logPosition(false);
            }
        }
    }


}
