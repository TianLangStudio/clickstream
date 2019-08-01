package com.tianlangstudio.clickstream

import org.apache.spark.sql.{ForeachWriter, SparkSession}

case class ClickStreamLog(remoteAddr: String,remoteUser: String, timeLocal: String,reqMethod: String, queryString: String,schema: String,sid: String)
object ClickStreamApp {
  val nginxLogPattern = """(\d+.\d+.\d+.\d+)\^([-\w\d]+)\^\[(.*)\]\^"(\w+) /stat.png\?([^\^]+)& (\w+/[\d.]*)"\^\d*\^\d*\^"[^"]*"\^"[^"]*"\^"[^"]*"""".r
  val sidPattern = """.*&sid=([\w\d]+)&.*""".r
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ClickStream-TianlangStudio")
      .master("local[1]")
      .getOrCreate()

    import spark.implicits._

    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "192.168.0.102:9092")
      .option("subscribe", "clickstream")
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING)")
      .as[String]
      .map( {
        case nginxLogPattern(remoteAddr,remoteUser,timeLocal,reqMethod, queryString,schema) => {
          val sid = queryString match {
            case sidPattern(sid) => sid
            case _ => ""
          }
          List(remoteAddr,remoteUser, timeLocal, reqMethod, queryString, schema, sid)
        }
        case _ => List()
      }).filter(_.size > 0)

      println("recode");

    val query = df.writeStream
      .outputMode("append")/*.format("csv")
      .option("path", "/data/test/spark")
      .option("checkpointLocation", "/tmp/spark/test")*/
      .format("console")
      /*.option("startingOffsets", "earliest")*/
      .start()

     query.awaitTermination()
  }

}
