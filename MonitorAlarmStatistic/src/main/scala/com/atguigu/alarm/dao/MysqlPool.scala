package com.atguigu.alarm.dao

import java.sql.Connection

import com.atguigu.alarm.util.Conf
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.log4j.LogManager


/**
  * mysql连接池类
  */
class MysqlPool extends Serializable {

  @transient lazy val log = LogManager.getLogger(this.getClass)

  private val cpds = new ComboPooledDataSource(true)
  private val conf: Map[String, String] = Conf.mysqlConfig

  try{
    cpds.setJdbcUrl(conf.getOrElse("url","jdbc:mysql://hadoop102:3306/monitor_alarm?characterEncoding=UTF-8"))
    cpds.setDriverClass("com.mysql.jdbc.Driver")
    cpds.setUser(conf.getOrElse("username","root"))
    cpds.setPassword(conf.getOrElse("password","123456"))
    cpds.setInitialPoolSize(3)
    cpds.setMaxPoolSize(Conf.maxPoolSize)
    cpds.setMinPoolSize(Conf.minPoolSize)
    cpds.setAcquireIncrement(5)
    cpds.setMaxStatements(180)
    //最大空闲时间,25000秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0
    cpds.setMaxIdleTime(25000)
    // 检测连接配置
    cpds.setPreferredTestQuery("select id from word_count_201610 where id = 1")
    cpds.setIdleConnectionTestPeriod(18000)
  }catch {
    case e:Exception=>
      log.error("[MysqlPoolError]" , e)
  }

  def getConnection():Connection={
    try{
      cpds.getConnection
    }catch {
      case e : Exception =>
        log.error("MysqlPoolGetConnectionError" , e )
      null
    }
  }
}

object MysqlManager extends Serializable {
  var mysqlManager : MysqlPool = _
  def getMysqlManager:MysqlPool={
    synchronized{
      if (mysqlManager == null){
        mysqlManager = new MysqlPool
      }
    }
    mysqlManager
  }

  def main(args: Array[String]): Unit = {
    println(getMysqlManager.getConnection())
  }
}
