package com.atguigu.alarm.main

import java.util.Properties

import com.atguigu.alarm.dao.KafkaSink
import com.atguigu.alarm.entity.MonitorGame
import com.atguigu.alarm.service.{MysqlService, SegmentService}
import com.atguigu.alarm.util
import com.atguigu.alarm.util.{BroadcastWrapper, Conf}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.{LogManager, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 首先建立sparkStreaming和kafka的连接，然后将监控游戏库
  * 及KafkaSink对象广播到各个节点上，每个节点对接收到的数据进行过滤分词，
  * 最后将过滤分词后的数据输出到Kafka中，并且定期更新广播变量。
  */
object MonitorAlarmStatistic {
  @transient  val log: Logger = LogManager.getLogger(this.getClass)
  def main(args: Array[String]): Unit = {
    val ssc = createContext()
    ssc.start()
    ssc.awaitTermination()
  }

  def createContext():StreamingContext ={
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("MonitorAlarmStatistic")
      .setMaster(Conf.master)
      .set("spark.default.parallelism", Conf.parallelNum)
      .set("spark.streaming.concurrentJobs", Conf.concurrentJobs)
      .set("spark.executor.memory", Conf.executorMem)
      .set("spark.cores.max", Conf.coresMax)
      .set("spark.local.dir", Conf.localDir)
      .set("spark.streaming.kafka.maxRatePerPartition", Conf.perMaxRate)
//      .set("jars","/opt/module/datas/tasks/MonitorAlarmStatistic-jar-with-dependencies.jar")
      .set("jars","MonitorAlarmStatistic/target/MonitorAlarmStatistic-jar-with-dependencies.jar")
    val ssc = new StreamingContext(sparkConf,Seconds(Conf.interval))

    val kafkaParams: Map[String, Object] = Map[String, Object](
      "bootstrap.servers" -> Conf.brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> Conf.group,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)     //如果写成false，则打包时会报：the result type of an implicit conversion must be more specific than AnyRef
    )
    val kafkaDirectStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](Conf.topics, kafkaParams)
    )
    log.warn(s"Initial Done >>> topic : ${Conf.topics}\t group:${Conf.group}\t brokers:${Conf.brokers}")

    //广播监控游戏库,T中Long:时间戳，Map[Int,MonitorGame] 代表，game_id和MonitorGame对象
    val MonitorGame: BroadcastWrapper[(Long, Map[Int, MonitorGame])] = util.BroadcastWrapper[(Long, Map[Int, MonitorGame])](
      ssc,
      (System.currentTimeMillis(), MysqlService.getGames()) //_v:T
    )
    //广播KafkaSink
    val kafkaProducer :Broadcast[KafkaSink[String, String]]= {
      val kafkaProducerConfig = {
        val p = new Properties()
        p.setProperty("bootstrap.servers",Conf.brokers)
        p.setProperty("key.serializer","org.apache.kafka.common.serialization.StringDeserializer")
        p.setProperty("value.serializer","org.apache.kafka.common.serialization.StringDeserializer")
        p
      }
      log.warn("kafka producer init done")
      val value: Broadcast[KafkaSink[String, String]] = ssc.sparkContext.broadcast(KafkaSink[String,String](kafkaProducerConfig))
      value
    }

    //经过分词得到新的stream
    val segmentedStream: DStream[(Int, String)] = kafkaDirectStream.map(_.value()).transform(rdd => {
      //定期更新监控游戏库
      if ((System.currentTimeMillis() - MonitorGame.value._1) > Conf.updateFreq) {
        MonitorGame.update((System.currentTimeMillis(), MysqlService.getGames()), true)
        log.warn("[BroadcastWrapper] MonitorGame updated")
      }
      rdd.flatMap { json =>
        SegmentService.mapSegent(json, MonitorGame.value._2)
      }
    }
    )
    //将分过词的stream输出到kafka中outTopics中
    segmentedStream.foreachRDD(rdd=>{
        if(!rdd.isEmpty()){
          rdd.foreach(record=>{
            kafkaProducer.value.send(Conf.outTopics,record._1.toString,record._2)
            //输出一条，打印一条日志
            log.warn(s"[kafkaOutput] output to ${Conf.outTopics} game_id : ${record._1}")
          })
        }
      }
    )
    ssc
  }



}
