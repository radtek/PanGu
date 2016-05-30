package nirvana.spark.services.bianjian

import java.io.File

import kafka.serializer.StringDecoder
import monad.support.MonadSupportConstants
import monad.support.services.XmlLoader
import nirvana.spark.config.NirvanaSparkConfig
import nirvana.spark.services.{ExtractFeatureService, DecompressImageService}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, StreamingContext}

import scala.io.Source

/**
 * bing data stream process
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-30
 */
object BianjianStream {

  def createStreamContext(checkpointDirectory:Option[String],parameter:NirvanaSparkConfig): StreamingContext ={
    val conf = new SparkConf().setAppName(parameter.appName)
      .setMaster(parameter.masterServer)
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    //spark parell
//    conf.set("spark.default.parallelism","1000")
    conf.set("spark.driver.host",parameter.host)
    val ssc =  new StreamingContext(conf, Minutes(5))
    checkpointDirectory.foreach(ssc.checkpoint)


    val kafkaParams = Map[String, String]("metadata.broker.list" -> parameter.kafkaServer,"auto.offset.reset"->"smallest")

    KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder] (ssc, kafkaParams, Set("wsq"))
      .repartition(parameter.partitionsNum)
      .map(_._2)
      .flatMap(BianjianImageProviderService.requestWsqData(parameter, _))
      .flatMap(x=> DecompressImageService.requestDecompress(parameter, x._1, x._2))
      .flatMap(x=> ExtractFeatureService.requestExtract(parameter, x._1, x._2))
      .foreachRDD { rdd =>
        rdd.foreachPartition(BianjianPartitionRecordsSaver.savePartitionRecords(parameter))
      }

    ssc

  }
  def main(args:Array[String]): Unit ={
    if(args.length != 1){
      println("please add parameter: <configuration file path>")
      return
    }
    val content = Source.fromFile(new File(args(0)),MonadSupportConstants.UTF8_ENCODING).mkString
    val config = XmlLoader.parseXML[NirvanaSparkConfig](content, xsd = Some(getClass.getResourceAsStream("/nirvana/spark/nirvana-spark.xsd")))
    val checkpointDirectory = config.hdfsServer
    val ssc =
    if(checkpointDirectory == "None"){
      createStreamContext(None,config)
    }else {
      StreamingContext.getOrCreate(checkpointDirectory, () => {
        createStreamContext(Some(checkpointDirectory), config)
      })
    }

    ssc.start()
    ssc.awaitTermination()
  }
}

