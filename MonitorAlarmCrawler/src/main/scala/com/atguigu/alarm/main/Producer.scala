package com.atguigu.alarm.main

import java.util.Properties

//添加spray_json方便Map转换为json字符串
import spray.json._
import DefaultJsonProtocol._

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer {
  def main(args: Array[String]): Unit = {
    //从命令行中读取相关参数
    //1.读取爬虫爬取页数
    val pageNumPerGame = args(0).toInt
    //2.主题
    val topic = args(1)
    //3.broker
    val broker = args(2)

    //设置kafka配置项
    val props = new Properties()
    props.put("bootstrap.servers",broker)
    props.put("client.id","monitorAlarmCrawler")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String,String](props)
    //从taptap上爬取用户评论数据（game_id,reviews）
    val crawlerData: Map[Int, List[String]] = Crawler.crawData(pageNumPerGame)

    //用来记录传输到kafka的记录总数
    var events = 0
    crawlerData.foreach(data =>{
      val (game_id,reviews) = data
      reviews.foreach(review=>{
        //防止中文乱码，转换为UTF-8
        val revUtf8 = new String(review.getBytes(),0,review.length,"UTF-8")
        //key为game_id,value为JSON字符串
        val record = new ProducerRecord[String,String](topic,game_id.toString,Map("game_id"->game_id.toString,"review"->revUtf8).toJson.toString())
        //写入kafka
        producer.send(record)
        events += 1
      })
    })
    System.out.println("总共发送记录数为：" + events)
    producer.close()
  }
}
