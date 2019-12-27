import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.mutable.ListBuffer

object maptest {
  def main(args: Array[String]): Unit = {
    var data = Map[Int,List[String]]()

    val reviews = ListBuffer[String]("aaa","bbb","ccc")
    val reviews2 = ListBuffer[String]("111","222","333")
    data += (2301->reviews.toList)
    data += (2222 ->reviews2.toList )
    val list1: List[String] = data(2301)
//    println(list1(0))
//    println(data(2301)(0))
    classOf[StringDeserializer]
    println(classOf[StringDeserializer].toString.split(" ")(1))
    println(classOf[StringDeserializer])
  }
}
