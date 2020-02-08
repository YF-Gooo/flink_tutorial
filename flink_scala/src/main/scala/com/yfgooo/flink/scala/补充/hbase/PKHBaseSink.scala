package com.yfgooo.flink.hbase

import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala.hadoop.mapreduce.HadoopOutputFormat
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Mutation, Put}
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.flink.api.scala._
import scala.collection.mutable.ListBuffer

/**
  * 使用Flink将数据写入HBase中
  */
object PKHBaseSink {

  /**
    * 把DataSet转成HBase能支持的数据类型
    */
  def convertToHBase(input: DataSet[(String, String, Int, String)]) = {
    input.map(new RichMapFunction[(String, String, Int, String),(Text,Mutation)] {

      val cf = "o".getBytes

      override def map(value: (String, String, Int, String)): (Text, Mutation) = {
        val id = value._1
        val name = value._2
        val age = value._3
        val city = value._4

        val text = new Text(id)
        val put = new Put(id.getBytes)

        if(StringUtils.isNotEmpty(name)) {
          put.addColumn(cf, Bytes.toBytes("name"),Bytes.toBytes(name))
        }

        if(StringUtils.isNotEmpty(age+"")) {
          put.addColumn(cf, Bytes.toBytes("age"), Bytes.toBytes(age.toString))
        }

        if(StringUtils.isNotEmpty(city)) {
          put.addColumn(cf, Bytes.toBytes("city"), Bytes.toBytes(city))
        }
        (text, put)
      }
    })
  }

  def main(args: Array[String]): Unit = {

    val environment = ExecutionEnvironment.getExecutionEnvironment

    /**
      * 准备测试数据
      */
    val students = ListBuffer[(String, String, Int, String)]()
    for (i <- 1 to 10) {
      students.append((i + "", "PK" + i, 30 + i, "beijing" + i))
    }

    val input = environment.fromCollection(students)

    /**
      * TODO... 是把input这个DataSet写入HBase中
      *
      */
    val result = convertToHBase(input)

    val configuration = HBaseConfiguration.create()
    configuration.set("hbase.zookeeper.quorum","ruozedata001")
    configuration.set("hbase.zookeeper.property.clientPort","2181")
    configuration.set(TableOutputFormat.OUTPUT_TABLE, "pk_stus2")
    configuration.set("mapreduce.output.fileoutputformat.outputdir","/tmp")


    val job = Job.getInstance(configuration)
    result.output(new HadoopOutputFormat[Text,Mutation](new TableOutputFormat[Text](),job))

    environment.execute(this.getClass.getSimpleName)
  }

}
