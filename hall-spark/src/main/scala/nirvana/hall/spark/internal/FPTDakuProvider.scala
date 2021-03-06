package nirvana.hall.spark.internal

import java.io.{ByteArrayInputStream, File}

import nirvana.hall.c.AncientConstants
import nirvana.hall.c.services.gfpt4lib.fpt4code._
import nirvana.hall.c.services.gfpt4lib.{FPTFile, fpt4code}
import nirvana.hall.c.services.gloclib.glocdef.GAFISIMAGESTRUCT
import nirvana.hall.c.services.kernel.FPTLDataToMNTDISP
import nirvana.hall.c.services.kernel.mnt_checker_def.MNTDISPSTRUCT
import nirvana.hall.extractor.internal.FPTLatentConverter
import nirvana.hall.protocol.extract.ExtractProto.ExtractRequest.FeatureType
import nirvana.hall.protocol.extract.ExtractProto.FingerPosition
import nirvana.hall.spark.config.NirvanaSparkConfig
import nirvana.hall.spark.services.FptPropertiesConverter.TemplateFingerConvert
import nirvana.hall.spark.services.SparkFunctions._
import nirvana.hall.spark.services._
import org.apache.commons.io.{FileUtils, IOUtils}

import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

/**
  * Created by wangjue on 2016/9/29.
  */
class FPTDakuProvider extends ImageProvider{
  private lazy val imageFileServer = SysProperties.getPropertyOption("fpt.file.server")
  private lazy val reExtract = SysProperties.getBoolean("extractor.is.reset",defaultValue = false)
  override def requestImageByBMP(parameter:NirvanaSparkConfig,pkId:String): Option[(StreamEvent,TemplateFingerConvert,GAFISIMAGESTRUCT,GAFISIMAGESTRUCT)] = {null}
  override def requestData(parameter: NirvanaSparkConfig, message: String): Seq[(StreamEvent, TemplateFingerConvert, GAFISIMAGESTRUCT, GAFISIMAGESTRUCT)] = {ArrayBuffer[(StreamEvent, TemplateFingerConvert, GAFISIMAGESTRUCT, GAFISIMAGESTRUCT)]()}

  def requestImage(parameter:NirvanaSparkConfig,filePathSuffix:String): Seq[(StreamEvent,GAFISIMAGESTRUCT)] ={
    def fetchFPT(seq:Int): Seq[(StreamEvent, GAFISIMAGESTRUCT)] = {
      try {
        /*val path = "http://10.1.7.204/fpt/out_700_950w/P1201606301467274240740.fpt"
        val data = SparkFunctions.httpClient.download(path)*/
        //val data = SparkFunctions.httpClient.download(imageFileServer.get + filePathSuffix)
        var path = filePathSuffix
        if (!filePathSuffix.startsWith("http")) path = imageFileServer.get + filePathSuffix
        //if (filePathSuffix.indexOf("http") == -1) path = imageFileServer.get + filePathSuffix
        val data = SparkFunctions.httpClient.download(path)
        val fpt = FPTFile.parseFromInputStream(new ByteArrayInputStream(data), AncientConstants.GBK_ENCODING)

        /*val is = FileUtils.openInputStream(new File("C:\\Users\\wangjue\\Desktop\\fail_FPT\\R1111326224222286523227.fpt"))
        val fpt= FPTFile.parseFromInputStream(is,AncientConstants.GBK_ENCODING)
        IOUtils.closeQuietly(is)*/

        val buffer = ArrayBuffer[(StreamEvent, GAFISIMAGESTRUCT)]()
        if (filePathSuffix.isEmpty) return buffer.toSeq
        val filePath : String = path
        var personId: String = null
        fpt match {
          case Left(fpt3) =>
            //assert(fpt3.fileLength.toInt == fpt3.getDataSize,"fpt3 fileLength != dataSize")
            val tpCounts = fpt3.tpCount
            var tpCount = 0
            if (tpCounts!=null && !"".equals(tpCounts))
              tpCount = tpCounts.toInt
            val lpCounts = fpt3.lpCount
            var lpCount = 0
            if (lpCounts!=null && !"".equals(lpCounts))
              lpCount = lpCounts.toInt
            if (tpCount > 0) {
              assert(tpCount == fpt3.logic3Recs.length)
              fpt3.logic3Recs.foreach { tp =>
                val fingerCount = tp.sendFingerCount.toInt
                assert(fingerCount == tp.fingers.length)
                personId = tp.personId
                assert(personId != null, "person id is null")
                //save person base information
                val hasPerson = GafisPartitionRecordsSaver.queryPersonById(personId)
                if (hasPerson.isEmpty) {
                  val person = FptPropertiesConverter.fpt3ToPersonConvert(tp,filePath)
                  GafisPartitionRecordsDakuSaver.savePersonInfo(person)
                }
                var list : List[Array[Int]] = List()
                if (reExtract) //reset finger mnt
                  GafisPartitionRecordsDakuSaver.deleteTemplateFingerMntOrBin(personId)
                else
                  list = GafisPartitionRecordsDakuSaver.queryFingerFgpAndFgpCaseByPersonId(personId)
                tp.fingers.foreach { tData =>
                  if (tData.imgData != null && tData.imgData.length > 0) {
                    val tBuffer = createImageEvent(filePath, personId, tData, list)
                    if (tBuffer != null)
                      buffer += tBuffer
                  }
                }
              }
            }
            if (lpCount > 0) { //process latent FPT
              fpt3.logic2Recs.foreach { lp =>
                val caseId = lp.caseId
                assert(caseId != null && !"".equals(caseId), "case id is null")
                val latentCase = FptPropertiesConverter.fpt3ToLatentCaseConvert(lp)

                lp.fingers.foreach{ lData=>
                  var seqNo = lData.fingerNo.toInt.toString
                  var cardId = lData.fingerId
                  if (cardId == null || "".equals(cardId)) {
                    if (seqNo != null && !"".equals(seqNo)) {
                      if (seqNo.toInt < 10)
                        seqNo = "0" + seqNo
                      cardId = caseId + seqNo
                    } else
                      cardId = null
                  }
                  assert(cardId != null && !"".equals(cardId), "card id is null")
                  val latentFinger = FptPropertiesConverter.fpt3ToLatentFingerConvert(lData,filePath,caseId,cardId.trim,seqNo)
                  if (lData != null && lData.featureCount.toInt > 0) {
                    val disp = FPTLDataToMNTDISP.convertFPT03ToMNTDISP(lData)
                    val feature = createImageLatentEvent(disp)
                    val GFSFEATURE = fpt4code.FPTFingerLDataToGafisImage(lData)
                    GFSFEATURE.bnData = feature
                    GFSFEATURE.stHead.nImgSize = feature.length
                    val latentFingerFeature = FptPropertiesConverter.fptToLatentFingerFeatureConvert(cardId,GFSFEATURE.toByteArray(),lData.extractMethod)
                    latentFinger.LatentFingerFeatures =  latentFingerFeature :: Nil
                  }
                  if (latentCase.latentFingers == null)
                    latentCase.latentFingers = latentFinger :: Nil
                  else
                    latentCase.latentFingers = latentCase.latentFingers ::: latentFinger :: Nil
                }
                GafisPartitionRecordsDakuSaver.saveLatent(latentCase)
              }
            }
          case Right(fpt4) =>
            /*if (fpt4.fileLength.toInt != fpt4.getDataSize) {
              println("fileLength="+fpt4.fileLength.toInt+"|dataSize="+fpt4.getDataSize+"|filePath="+filePath)
            }
            assert(fpt4.fileLength.toInt == fpt4.getDataSize,"fpt4 fileLength != dataSize")*/
            val tpCounts = fpt4.tpCount
            var tpCount = 0
            if (tpCounts!=null && !"".equals(tpCounts))
              tpCount = tpCounts.toInt
            val lpCounts = fpt4.lpCount
            var lpCount = 0
            if (lpCounts!=null && !"".equals(lpCounts))
              lpCount = lpCounts.toInt
            if (tpCount > 0) {
              assert(tpCount == fpt4.logic02Recs.length)
              fpt4.logic02Recs.foreach { tp =>
                val fingerCount = tp.sendFingerCount.toInt
                assert(fingerCount == tp.fingers.length)
                personId = tp.personId
                assert(personId != null, "person id is null")
                //save person base information
                val hasPerson = GafisPartitionRecordsDakuSaver.queryPersonById(personId)
                if (hasPerson.isEmpty) {
                  val person = FptPropertiesConverter.fpt4ToPersonConvert(tp, filePath)
                  GafisPartitionRecordsDakuSaver.savePersonInfo(person)
                }
                var list : List[Array[Int]] = List()
                if (reExtract) //reset finger mnt
                  GafisPartitionRecordsDakuSaver.deleteTemplateFingerMntOrBin(personId)
                else
                  list = GafisPartitionRecordsDakuSaver.queryFingerFgpAndFgpCaseByPersonId(personId)
                tp.fingers.foreach { tData =>
                  if (tData.imgData != null && tData.imgData.length > 0) {
                    val tBuffer = createImageEvent(filePath, personId, tData, list)
                    if (tBuffer != null)
                      buffer += tBuffer
                  }
                }
              }
            }
            if (lpCount > 0) { //process latent FPT
              fpt4.logic03Recs.foreach { lp =>
                val caseId = lp.caseId
                assert(caseId != null && !"".equals(caseId), "case id is null")
                val latentCase = FptPropertiesConverter.fpt4ToLatentCaseConvert(lp)

                lp.fingers.foreach{ lData=>
                  var seqNo = lData.fingerNo.toInt.toString
                  var cardId = lData.fingerId
                  if (cardId == null || "".equals(cardId)) {
                    if (seqNo != null && !"".equals(seqNo)) {
                      if (seqNo.toInt < 10)
                        seqNo = "0" + seqNo
                      cardId = caseId + seqNo
                    } else
                      cardId = null
                  }
                  assert(cardId != null && !"".equals(cardId), "card id is null")
                  val latentFinger = FptPropertiesConverter.fpt4ToLatentFingerConvert(lData,filePath,caseId,cardId.trim,seqNo)
                  if (lData != null && lData.featureCount.toInt > 0) {
                    val disp = FPTLDataToMNTDISP.convertFPT03ToMNTDISP(lData)
                    val feature = createImageLatentEvent(disp)
                    val GFSFEATURE = fpt4code.FPTFingerLDataToGafisImage(lData)
                    GFSFEATURE.bnData = feature
                    GFSFEATURE.stHead.nImgSize = feature.length
                    val latentFingerFeature = FptPropertiesConverter.fptToLatentFingerFeatureConvert(cardId,GFSFEATURE.toByteArray(),lData.extractMethod)
                    latentFinger.LatentFingerFeatures =  latentFingerFeature :: Nil
                  }
                  if (latentCase.latentFingers == null)
                    latentCase.latentFingers = latentFinger :: Nil
                  else
                    latentCase.latentFingers = latentCase.latentFingers ::: latentFinger :: Nil
                }
                GafisPartitionRecordsDakuSaver.saveLatent(latentCase)
              }
            }
        }
        buffer.toSeq
      }catch{
        case e:CallRpcException=>
          //try 4 times to fetch fpt file
          if(seq == 4)  reportException(e)  else fetchFPT(seq+1)
        case NonFatal(e)=>
          reportException(e)
      }
    }
    def reportException(e: Throwable) = {
      e.printStackTrace(System.err)
      var path = filePathSuffix
      if (!filePathSuffix.startsWith("http")) path = imageFileServer.get + filePathSuffix
      val event = StreamEvent(path, "",  FeatureType.FingerTemplate, FingerPosition.FINGER_UNDET,"","","")
      reportError(parameter, RequestRemoteFileError(event, e.toString))
      Nil
    }

    fetchFPT(1)
  }

  def createImageEvent(filePath: String, personId: String, tData: FPTFingerData, list : List[Array[Int]]): (StreamEvent,GAFISIMAGESTRUCT) = {
    val gafisImg = fpt4code.FPTFingerDataToGafisImage(tData)
    val fgp = getFingerPosition(tData.fgp.toInt)
    val fgpCase = gafisImg.stHead.bIsPlain
    val isExists = list.exists(x=> x(0) == fgp.getNumber && x(1) == fgpCase)
    if (!isExists) {
      val event = new StreamEvent(filePath, personId, FeatureType.FingerTemplate, fgp, "", "", "")
      (event, gafisImg)
    } else null
  }
  def createImageLatentEvent(disp: MNTDISPSTRUCT): Array[Byte] = {
    SparkFunctions.loadExtractorJNI()
    val latentFeature = FPTLatentConverter.convert(disp)
    latentFeature.toByteArray()
  }
}
