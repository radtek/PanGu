package nirvana.hall.c.services.gfpt4lib

import java.io.{File, FileInputStream}

import nirvana.hall.c.AncientConstants
import nirvana.hall.c.services.AncientData._
import nirvana.hall.c.services.gfpt4lib.FPT3File.FPT3File
import nirvana.hall.c.services.gfpt4lib.FPT4File.FPT4File
import org.apache.commons.io.{FileUtils, IOUtils}
import org.junit.{Assert, Test}

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-27
 */
class FPTFileTest {
  @Test
  def test_convert: Unit = {
    val files = FileUtils.listFiles(new File("/Users/jcai/Downloads/fpt_failed"), Array[String]("fpt", "FPT", "fptt"), true)
    val it = files.iterator()
    while(it.hasNext){
      val file = it.next()
      println("processing "+file.getAbsolutePath)
      val is = new FileInputStream(file)
      val result = FPTFile.parseFromInputStream(is,AncientConstants.GBK_ENCODING)
      IOUtils.closeQuietly(is)
      val bytes = result match{
        case Left(fpt3)=>
          fpt3.fs = FPTFile.FS
          fpt3.toByteArray(AncientConstants.GBK_ENCODING)
        case Right(fpt4)=>
          fpt4.toByteArray(AncientConstants.GBK_ENCODING)
      }
      FileUtils.writeByteArrayToFile(new File(file.getAbsolutePath+".converted"),bytes)
    }
  }
  @Test
  def test_performance: Unit ={
    val files = FileUtils.listFiles(new File("/Users/jcai/Downloads/fpt-files"),Array[String]("fpt","FPT","fptt"),true)
    val it = files.iterator()
    while(it.hasNext){
      val file = it.next()
//      println("processing "+file.getAbsolutePath)
      try {
        val is = new FileInputStream(file)
        val result = FPTFile.parseFromInputStream(is)
        IOUtils.closeQuietly(is)
        result match{
          case Right(fpt4)=>
            assert(fpt4.fileLength.toInt == fpt4.getDataSize)
            val tpCount = fpt4.tpCount.toInt
            if(tpCount > 0){
              assert(tpCount == fpt4.logic02Recs.size)
              var method:String = null
              fpt4.logic02Recs.foreach{tp=>
                val fingerCount = tp.sendFingerCount.toInt
                assert(fingerCount == tp.fingers.size)
                tp.fingers.foreach{tData=>
                  method = tData.imgCompressMethod
                  //println("compress method "+tData.imgCompressMethod)
                }
              }
              if(method == "1700")
              println(method+" "+file.getPath)
            }
          case Left(fpt3) =>
            val tpCount = fpt3.tpCount.toInt
            if(tpCount >0) {
              var method:String = null
              assert(fpt3.tpCount.toInt == fpt3.logic3Recs.size)
              fpt3.logic3Recs.foreach{tp=>
                val fingerCount = tp.sendFingerCount.toInt
                assert(fingerCount == tp.fingers.size)
                tp.fingers.foreach{tData=>
                  method = tData.imgCompressMethod
                  //println("compress method "+tData.imgCompressMethod)
                }
              }
              if(method == "1700")
                println(method+" "+file.getAbsolutePath)
            }
        }
      }catch{
        case e:Throwable=>
//          println(file.getAbsolutePath+" \n\te:"+e.toString)
      }
    }
  }
  @Test
  def test_parse: Unit ={
    val is = getClass.getResourceAsStream("/3100002009627747H_20170521071902867.fpt")
    val fpt=FPTFile.parseFromInputStream(is)
    Assert.assertTrue(fpt.isLeft)
    val is2 = getClass.getResourceAsStream("/fpt4.fpt")
    val fpt2=FPTFile.parseFromInputStream(is2)
    Assert.assertTrue(fpt2.isRight)
  }
  @Test
  def test_read_no_tpCount: Unit ={
    val fpt3 = new FPT3File
    fpt3.fromStreamReader(getClass.getResourceAsStream("/no-tp-count.fpt3"),AncientConstants.GBK_ENCODING)
    Assert.assertEquals(1,fpt3.logic3Recs.size)
    val fpt3_2= new FPT3File().fromByteArray(fpt3.toByteArray())
    Assert.assertEquals(fpt3.fileLength.toInt,fpt3.getDataSize)
  }
  @Test
  def test_read_fpt3: Unit ={
    val bytes = IOUtils.toByteArray(getClass.getResourceAsStream("/fpt3.fpt"))
    val fpt3 = new FPT3File
    fpt3.fromStreamReader(getClass.getResourceAsStream("/fpt3.fpt"),AncientConstants.GBK_ENCODING)
    Assert.assertEquals(1,fpt3.logic3Recs.size)
    val fpt3_2= new FPT3File().fromByteArray(fpt3.toByteArray())
    Assert.assertEquals(fpt3.fileLength.toInt,fpt3.getDataSize)
  }
  @Test
  def test_read_fpt4:Unit={
    var fpt4 = new FPT4File
    fpt4.fromStreamReader(getClass.getResourceAsStream("/fpt4.fpt")) //,AncientConstants.GBK_ENCODING)
    Assert.assertEquals(fpt4.logic02Recs.head.fingers.size ,fpt4.logic02Recs.head.sendFingerCount.toInt)
    Assert.assertEquals(fpt4.getDataSize,fpt4.fileLength.toInt)
    val bytes = fpt4.toByteArray()
    Assert.assertEquals(fpt4.getDataSize,bytes.size)

    fpt4 = new FPT4File
    fpt4.fromByteArray(bytes)
    Assert.assertEquals(fpt4.logic02Recs.head.fingers.size ,fpt4.logic02Recs.head.sendFingerCount.toInt)
    Assert.assertEquals(fpt4.getDataSize,fpt4.fileLength.toInt)
  }
  @Test
  def test_read_fpt4_2:Unit={
    val fpt4 = new FPT4File
    fpt4.fromStreamReader(getClass.getResourceAsStream("/fpt4_2.fpt"),AncientConstants.GBK_ENCODING)
    Assert.assertEquals(fpt4.getDataSize,fpt4.fileLength.toInt)
    val bytes = fpt4.toByteArray(encoding=AncientConstants.GBK_ENCODING)
    Assert.assertEquals(fpt4.getDataSize,bytes.size)
  }
  @Test
  def test_read_crash: Unit ={
    val fpt4 = new FPT4File
    val fileName ="/crash_fpt/B6220161123125135823645.FPT"
    try {
      fpt4.fromStreamReader(getClass.getResourceAsStream(fileName), AncientConstants.GBK_ENCODING)
    }catch{
      case e:Throwable=>
        logger.error(e.getMessage,e)
    }
    val head = fpt4.head
  }


  @Test
  def test_filterFPT: Unit ={
    val files = FileUtils.listFiles(new File("E:\\20170525"),Array[String]("fpt","FPT","fptt"),true)
    val it = files.iterator()
    while(it.hasNext){
      val file = it.next()
      try {
        val is = new FileInputStream(file)
        val result = FPTFile.parseFromInputStream(is)
        IOUtils.closeQuietly(is)
        result match{
          case Right(fpt4)=>
            fpt4.logic02Recs.head.fingers.foreach { (f: FPT4File.FingerTData)
            => f.customInfoLength = "0" //自定义信息长度	不能为空 必须为数字
              f.imgHorizontalLength = "640" //    图像水平方向长度	必须为640
              f.imgVerticalLength = "640" //      图像垂直方向长度	必须为640
              f.dpi = "500" //      图像分辨率	必须为500
              if(!f.imgCompressMethod.startsWith("14")){
                f.imgCompressMethod = "1419" //      图像压缩方法代码	不能为空 必须为14开头的WSQ压缩方法
              }
            }
            FileUtils.writeByteArrayToFile(new File(file.getAbsolutePath)
              ,fpt4.toByteArray(AncientConstants.GBK_ENCODING))

          case Left(fpt3) =>
            println("No support FPT V3.0")
        }
      }catch{
        case e:Throwable=>
        //          println(file.getAbsolutePath+" \n\te:"+e.toString)
      }
    }
  }
}
