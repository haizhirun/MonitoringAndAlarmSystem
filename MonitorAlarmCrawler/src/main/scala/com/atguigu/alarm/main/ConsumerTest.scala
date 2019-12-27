package com.atguigu.alarm.main

import java.util.concurrent.Executors
import java.util.{Collections, Properties}

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}

import scala.collection.JavaConversions._
//hadoop102:9092,hadoop103:9092,hadoop104:9092 test_group monitorAlarm
class ConsumerTest(val brokers:String,val groupId:String,val topic:String ) {

  val props = createConsumerConfg(brokers,groupId)

  val consumer = new KafkaConsumer[String,String](props)

  def createConsumerConfg(brokers: String,groupId: String):Properties={
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,brokers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG,groupId)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,"30000")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer")
    props
  }
  //程序拉取数据主要方法
  def run(): Unit ={
    consumer.subscribe(Collections.singletonList(this.topic))

    Executors.newSingleThreadExecutor().execute(new Runnable {
      override def run(): Unit = {
        while (true){
          val records: ConsumerRecords[String, String] = consumer.poll(1000)
          //能够遍历records,要导入import scala.collection.JavaConversions._这句，否则报错
          for(record <- records){
              System.out.println("Received message: " + record.key() + ", " + record.value())
          }
        }
      }
    })
  }

}

/**
  * 1
  * monitorAlarm
  * hadoop102:9092,hadoop103:9092,hadoop104:9092
  * monitorAlarmCrawler
  * hadoop102:9092,hadoop103:9092,hadoop104:9092 monitorAlarmCrawler monitorAlarm
  */
object ConsumerTest{
  def main(args: Array[String]): Unit = {
    val consumer = new ConsumerTest(args(0),args(1),args(2))
    consumer.run()
  }
}
