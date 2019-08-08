package com.tianlangstudio.clickstream

import org.apache.spark.sql.SparkSession

object ClickStreamStatApp {
      def main(args: Array[String]): Unit = {
        import org.apache.spark.sql.functions._
        import org.apache.spark.sql.SparkSession

        val spark = SparkSession
          .builder
          .appName("ClickStreamStatApp")
          .master("local[1]")
          .getOrCreate()

        import spark.implicits._
        val df = spark
          .readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", "192.168.55.106 :9092")
          //.option("kafka.bootstrap.servers", "192.168.0.102:9092")
          //offset
          .option("subscribe", "etl_clickstream")
          .load()

        df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
          .as[(String, String)]
          .select(split('value, "\001")(0) as "sid",
            //23/Jul/2019:10:05:59 +0000
            to_timestamp(split('value, "\001")(1), "dd/MMM/yyyy:HH:mm:ss z") as "time_local")
          .withWatermark("time_local", "20 seconds")
          .groupBy(window('time_local, "10 seconds"), 'sid)
          .count()
          .writeStream
          .format("console")
          //.outputMode("complete")
          .start()
          .awaitTermination()
      }
}
