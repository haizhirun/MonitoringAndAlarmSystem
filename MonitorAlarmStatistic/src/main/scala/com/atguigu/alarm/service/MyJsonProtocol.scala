package com.atguigu.alarm.service

import com.atguigu.alarm.entity.Record
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * 将从kafka中接收来的数据（都是json字符串），对其进行JSON解析
  */
object MyJsonProtocol extends DefaultJsonProtocol{
  implicit val docFormat: RootJsonFormat[Record] = jsonFormat2(Record)
}

object JsonParse extends Serializable {
  import spray.json._
  import MyJsonProtocol._
  //将Record对象转换为JSON字符串
  def record2Json(doc:Record):String={
    doc.toJson.toString()
  }
  //将JSON字符串转换为Record对象
  def json2Record(json:String):Record={
    json.parseJson.convertTo[Record]
  }
}
