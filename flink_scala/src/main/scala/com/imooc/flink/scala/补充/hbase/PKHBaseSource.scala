package com.yfgoo.flink.hbase
import org.apache.flink.api.scala._
import org.apache.flink.addons.hbase.TableInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HTable, Result, Scan}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.flink.api.java.tuple.Tuple4
import org.apache.flink.configuration.Configuration
/**
* 使用Flink将数据从HBase中读出来
*/
object PKHBaseSource {
  def main(args: Array[String]): Unit = {
    val environment = ExecutionEnvironment.getExecutionEnvironment

    val cf = "o".getBytes

    val result = environment.createInput(new TableInputFormat[Tuple4[String, String, Int, String]] {
      def createTable() = {
        val configuration = HBaseConfiguration.create()
        configuration.set("hbase.zookeeper.quorum", "ruozedata001")
        configuration.set("hbase.zookeeper.property.clientPort", "2181")
        new HTable(configuration, getTableName)
      }

      override def configure(parameters: Configuration): Unit = {
        table = createTable
        if (table != null) {
          scan = getScanner
        }
      }

      override def mapResultToTuple(result: Result): Tuple4[String, String, Int, String] = {
        new Tuple4(Bytes.toString(result.getRow),
          Bytes.toString(result.getValue(cf, "name".getBytes())),
          Bytes.toString(result.getValue(cf, "age".getBytes())).toInt,
          Bytes.toString(result.getValue(cf, "city".getBytes()))
        )
      }

      override def getTableName: String = {
        "pk_stus2"
      }

      override def getScanner: Scan = {
        val scan = new Scan()
        scan.addFamily(cf)
        scan
      }
    })
    result.print()

    environment.execute(this.getClass.getSimpleName)
  }
}
