package com.atguigu.alarm.util

object Conf extends Serializable {

  //每隔多长时间更新监控游戏库
  val updateFreq = 3600000 //60min

  //spark configuration
  val master = "spark://hadoop102:7077"
  //默认值为/tmp。用于设定Spark的缓存目录，包括了mapper输出的文件，缓存到磁盘的RDD数据。最好将这个属性设定为访问速度快的本地磁盘。同Hadoop一样，可以用逗号分割来设定多个不同磁盘的目录。需要注意，在Spark 1.0和之后的版本，这个属性将会被SPARK_LOCAL_DIRS (Standalone, Mesos) 或者 LOCAL_DIRS (YARN) 环境变量替代。
  val localDir = "/opt/module/datas/sparkData/dir/cache"
  val perMaxRate = "5"
  val interval = 3
  val parallelNum = "3"
  val executorMem = "1G"
  val concurrentJobs = "2"
  val coresMax = "2"

  //kafka configuration
  val brokers = "hadoop102:9092,hadoop103:9092,hadoop104:9092"
  val zk = "hadoop102:2181,hadoop103:2181,hadoop104:2181"
  val group = "MASGroup"
  val topics = List("monitorAlarm")
  val outTopics = "monitorAlarmOut"



  //mysql configuration
  val mysqlConfig: Map[String, String] = Map(
    "url" -> "jdbc:mysql://hadoop102:3306/monitor_alarm?characterEncoding=UTF-8",
    "username" -> "root",
    "password" -> "123456"
  )
  val maxPoolSize = 5
  val minPoolSize = 2
}
