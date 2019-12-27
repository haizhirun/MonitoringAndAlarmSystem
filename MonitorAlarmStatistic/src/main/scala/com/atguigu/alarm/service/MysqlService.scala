package com.atguigu.alarm.service

import java.sql.{ResultSet, Statement}

import com.atguigu.alarm.dao.MysqlManager
import com.atguigu.alarm.entity.MonitorGame
import org.apache.log4j.LogManager

/**
  * 从数据库中读取监控的游戏库
  */
object MysqlService extends Serializable {

  @transient lazy val log = LogManager.getLogger(this.getClass)

  /**
    * 加载监控游戏库
    */
  def getGames():Map[Int,MonitorGame]={
    val sql = "select * from monitor_games"
    val conn = MysqlManager.getMysqlManager.getConnection()
    val statement: Statement = conn.createStatement()

    try{
      var games = Map[Int,MonitorGame]()
      val rs: ResultSet = statement.executeQuery(sql)
      while(rs.next()){
        games += (rs.getInt("game_id")->MonitorGame(rs.getInt("game_id"),rs.getString("game_name")))
      }
      log.warn(s"[loadSuccess] load entity form db count : " + games.size)
      games
    }catch {
      case e:Exception =>
        log.error("[loadError]",e)
        Map[Int,MonitorGame]()
    }finally {
      statement.close()
      conn.close()
    }
  }
}
