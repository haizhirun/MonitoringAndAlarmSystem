package com.atguigu.alarm.util

object Conf {

  val mysqlConfig: Map[String, String] = Map(
    "url" -> "jdbc:mysql://hadoop102:3306/monitor_alarm?characterEncoding=UTF-8",
    "username" -> "root",
    "password" -> "123456"
  )
  val maxPoolSize = 5
  val minPoolSize = 2
}
