package com.tianlangstudio.clickstream

import java.util

import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark._
import org.apache.spark.streaming._

import scala.collection.JavaConverters._

object ClickStreamETLApp {
  val logNginxPattern = """(\d+.\d+.\d+.\d+)\^([-\w\d]+)\^\[(.*)\]\^"(\w+) /stat.png\?([^\^]+)& (\w+/[\d.]*)"\^\d*\^\d*\^"[^"]*"\^"[^"]*"\^"[^"]*"""".r
  val sidPattern = """.*&sid=([\w\d]+).*""".r
  val fieldDeli = "\001"
  val sourceTopic = "clickstream";
  val etlTopic = s"etl_$sourceTopic"
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("ClickStreamETL")
    val ssc = new StreamingContext(conf, Seconds(3))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "192.168.55.106:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "key.serializer" -> classOf[StringSerializer],
      "value.serializer" -> classOf[StringSerializer],
      "group.id" -> "click_stream_etl",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array(sourceTopic)
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    stream.map(record => record.value match  {
      case logNginxPattern(remoteAddr, remoteUser, timeLocal, reqMethod, queryString, schema) => {
        val sid = queryString match  {
          case sidPattern(sid) => sid
          case _ => ""
        }
        //Array("a","b","c").mkString(fieldDeli)
        s"""$sid$fieldDeli$timeLocal$fieldDeli$remoteAddr$fieldDeli$remoteUser$fieldDeli$reqMethod$fieldDeli$queryString$fieldDeli$schema"""
      }
      case _ => ""
    }).filter(!"".equals(_)).foreachRDD(strRdd => {
      strRdd.foreachPartition(strI => {
        val kafkaProducer = new KafkaProducer[String, String](kafkaParams.asJava)
        try {
          strI.foreach(value => {
            val producerRecord = new ProducerRecord(etlTopic,"",value)
            kafkaProducer.send(producerRecord)
          })
        }finally {
          if(kafkaProducer != null) {
            kafkaProducer.close()
          }
        }

      })
    })//.print()
    ssc.start()
    ssc.awaitTermination()
  }

}
