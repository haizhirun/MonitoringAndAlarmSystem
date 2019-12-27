package com.atguigu.alarm.service

import java.util

import com.atguigu.alarm.entity.MonitorGame
import com.huaban.analysis.jieba.{JiebaSegmenter, SegToken}
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import org.apache.log4j.{LogManager, Logger}
import spray.json._

import scala.collection.{JavaConversions, mutable}

/**
  * 将从kafka接收到的数据进行过滤和分词
  */
object SegmentService extends Serializable {

  @transient val log: Logger = LogManager.getLogger(this.getClass)

  /**
    * 将文本分词
    * @param
    * @return
    */
  def mapSegent(json:String,monitorGames:Map[Int,MonitorGame]):Option[(Int,String)]={
    json.parseJson.asJsObject.getFields("game_id","review") match {
      case Seq(JsString(game_id),JsString(review)) =>{
        if(!monitorGames.contains(game_id.toInt)){
          log.warn(s"[ignored] no need to monitor game_id:${game_id}")
          None
        }else{
          try{
            if(review.trim == ""){
              log.warn(s"[reviewEmptyError] json:${json}")
              None
            }else{
              val mg: MonitorGame = monitorGames.get(game_id.toInt).get
              //返回json结果
              val jo = JsObject(
                "game_id" -> JsNumber(mg.game_id),
                "review" -> JsString(review),
                "review_seg" -> JsString(segment(filter(review))),
                "game_name" -> JsString(mg.game_name)
              )
              log.warn(s"[Segment Success] game_id:${mg.game_id}\t game_name: ${mg.game_name}\t ${monitorGames.size}")
              Some((mg.game_id,jo.toString()))
            }
          }catch {
            case e:Exception=>
              log.error(s"[Segment Error] mapSegment error\tjson string: ${json}\treview: ${review}", e)
              None
          }

        }
      }
      case _ =>{
        log.warn(s"[Segment Match Failed] json parse match failed! error json is:\n${json}")
        None
      }

    }
  }

  def filter(s:String):String={
    s.replace("\t","")
  }

  def segment(review:String):String={
    val seg = new JiebaSegmenter
    /*
        jieba_index: 用于索引分词，分词粒度较细
        jieba_search: 用于查询分词，分词粒度较粗
        Search模式，用于对用户查询词分词
        Index模式，用于对索引文档分词
     */
    val ts: util.List[SegToken] = seg.process(review,SegMode.SEARCH)
    val words: mutable.MutableList[String] = mutable.MutableList[String]()
    for(t <- JavaConversions.asScalaBuffer(ts)){
      words += t.word
    }
    words.mkString("\t")
  }

  def main(args: Array[String]): Unit = {
    println(segment("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python和C++。"))
  }

}
