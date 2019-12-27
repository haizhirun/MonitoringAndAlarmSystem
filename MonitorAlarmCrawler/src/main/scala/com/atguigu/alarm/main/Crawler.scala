package com.atguigu.alarm.main

import java.sql.{Connection, ResultSet, Statement}

import com.atguigu.alarm.dao.{MysqlManager, MysqlPool}
import org.apache.log4j.LogManager
import kantan.xpath._
import kantan.xpath.implicits._
import kantan.xpath.nekohtml._

import scala.collection.mutable.ListBuffer
import scala.io.Source


object Crawler {
  @transient lazy val log = LogManager.getLogger(this.getClass)

  /**
    * 加载游戏库
    */
  def getGames :Map[Int,String] ={
    val preTime = System.currentTimeMillis()

    val sql = "select * from games"
    val conn: Connection = MysqlManager.getMysqlManager.getConnection
    val statement: Statement = conn.createStatement()

    try{
      val rs: ResultSet = statement.executeQuery(sql)
      var games = Map[Int,String]()
      while(rs.next()){
        games += (rs.getInt("game_id")->rs.getString("game_name"))
      }
      log.warn(s"[loadSuccess] load games from db count : ${games.size} \t time elapsed: ${System.currentTimeMillis() - preTime}")
      games
    }catch{
      case e:Exception =>
        log.error("[loadError]" , e)
        Map[Int,String]()
    }finally {
      statement.close()
      conn.close()
    }
  }

  /**
    * pageNum:需要爬取的评论页数
    * Int : game_id
    * List[String] : game_id下对应的评论
    * 从taptap: https://www.taptap.com/app/2301/review 上爬取用户评论数据
    * @return
    */
  def crawData(pageNum:Int):Map[Int,List[String]]={
    val games: Map[Int, String] = getGames

    var data = Map[Int,List[String]]()
    games.foreach(e =>{
        val (game_id,game_name) = e
        val reviews = ListBuffer[String]()
        for(page <- 1 until pageNum + 1){
          val url = s"https://www.taptap.com/app/${game_id}/review?order=default&page=$page#review-list"
          //https://www.taptap.com/app/83188/review?order=default&page=1#review-list
          println(url)
          val html = Source.fromURL(url).mkString
          val rs: XPathResult[List[String]] = html.evalXPath[List[String]](xp"//div[@class='item-text-body']/p")

          if(rs.isRight){
            reviews ++= rs.right.get
          }
          log.info(s"$game_name craw data size : ${reviews.size}")
          data += (game_id -> reviews.toList)
        }
      }
    )

    log.info(s"craw all data done, size : ${data.values.size}\n the first is ${data(2301)(0)}")
    data
  }

  def main(args: Array[String]): Unit = {
    crawData(1)
  }
}
