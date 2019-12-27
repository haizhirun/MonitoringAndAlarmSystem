package com.atguigu.alarm.util

import java.io.{ObjectInputStream, ObjectOutputStream}

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.StreamingContext

import scala.reflect.ClassTag

/**
  * 这个包装器使我们能够在DStreams的foreachRDD中更新广播变量，
  * 而不会遇到序列化问题
  * 利用unpersist()函数先将已经发布的广播变量删除，
  * 然后修改数据后重新进行广播
  */
case class BroadcastWrapper[T:ClassTag](
     @transient private val ssc:StreamingContext,
     @transient private val _v:T
     )extends Serializable {
  private var v = ssc.sparkContext.broadcast(_v)

  //更新广播变量
  def update(newValue:T,blocking:Boolean=false)={
    //删除rdd是否需要锁定
    v.unpersist(blocking)
    v = ssc.sparkContext.broadcast(newValue)
  }

  def value : T = v.value

  private def writeObject(out:ObjectOutputStream)={
    out.writeObject(v)
  }

  private def readObject(in:ObjectInputStream)={
    in.readObject().asInstanceOf[Broadcast[T]]
  }


}
