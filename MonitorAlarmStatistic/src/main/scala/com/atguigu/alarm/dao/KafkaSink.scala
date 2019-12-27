package com.atguigu.alarm.dao

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * 定义kafka相关参数
  * @param createProducer 创建kafka生产者函数
  * @tparam K key
  * @tparam V value
  */
class KafkaSink[K,V](createProducer:()=>KafkaProducer[K,V]) extends Serializable {
  lazy val producer = createProducer()

  def send(topic:String,key:K,value:V)={
    producer.send(new ProducerRecord[K,V](topic,key,value))
  }

  def send(topic:String,value:V)={
    producer.send(new ProducerRecord[K,V](topic,value))
  }
}

object KafkaSink extends Serializable {
  import scala.collection.JavaConversions._

  def apply[K,V](conf:Map[String,Object]):KafkaSink[K,V]={
    val createProducerFun = ()=>{
      val producer = new KafkaProducer[K,V](conf)
      sys.addShutdownHook{
        //确保在执行程序JVM关闭时，Kafka生产者在关闭之前,将所有缓冲的消息发送到Kafka
        producer.close()
      }
      producer
    }
    new KafkaSink(createProducerFun)
  }

  def apply[K,V](conf:Properties): KafkaSink[K,V] = apply(conf.toMap)

}
