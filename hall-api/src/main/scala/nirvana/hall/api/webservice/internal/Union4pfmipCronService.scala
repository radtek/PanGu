package nirvana.hall.api.webservice.internal


import javax.sql.DataSource

import com.google.protobuf.ByteString
import monad.support.services.LoggerSupport
import nirvana.hall.api.config.HallApiConfig
import nirvana.hall.api.services.sync.FetchQueryService
import nirvana.hall.api.services.{CaseInfoService, LPCardService, QueryService, TPCardService}
import nirvana.hall.api.webservice.services.union4pfmip
import nirvana.hall.api.webservice.util.FPTFileHandler
import nirvana.hall.c.services.gfpt4lib.FPT4File.{FPT4File, Logic02Rec}
import nirvana.hall.c.services.gfpt4lib.FPTFile
import nirvana.hall.protocol.api.FPTProto.{FingerFgp, ImageType, TPCard}
import nirvana.hall.protocol.extract.ExtractProto.ExtractRequest.FeatureType
import nirvana.hall.protocol.extract.ExtractProto.FingerPosition
import org.apache.tapestry5.ioc.annotations.PostInjection
import org.apache.tapestry5.ioc.services.cron.{CronSchedule, PeriodicExecutor}
import stark.webservice.services.StarkWebServiceClient

/**
  * 互查系统定时任务
  */
class Union4pfmipCronService(hallApiConfig: HallApiConfig,
                             tPCardService: TPCardService,
                             lPCardService: LPCardService,
                             caseInfoService: CaseInfoService,
                             queryService: QueryService,
                             fetchQueryService: FetchQueryService,
                             implicit val dataSource: DataSource) extends LoggerSupport{

  /**
    * 定时器，获取比对任务
    * @param periodicExecutor
    */
  @PostInjection
  def startUp(periodicExecutor: PeriodicExecutor): Unit = {
      periodicExecutor.addJob(new CronSchedule(hallApiConfig.webservice.union4pfmip.cron), "union4pfmip-cron", new Runnable {
        override def run(): Unit = {
          //发送请求获取任务
          val url = hallApiConfig.webservice.union4pfmip.url
          val targetNamespace = hallApiConfig.webservice.union4pfmip.targetNamespace
          val userid = hallApiConfig.webservice.union4pfmip.user
          val password = hallApiConfig.webservice.union4pfmip.password
          var taskControlID = ""
          val searchTaskService = StarkWebServiceClient.createClient(classOf[union4pfmip], url, targetNamespace)
          val taskDataHandler = searchTaskService.getSearchTask(userid, password)

            try{

              val taskFpt = FPTFile.parseFromInputStream(taskDataHandler.getInputStream)

              //解析比对任务FPT
              taskControlID = taskFpt.right.get.sid


              taskFpt match {
                case Left(fpt3) => throw new Exception("Not Support FPT-V3.0")
                case Right(fpt4) =>
                  if(fpt4.logic02Recs.length>0){
                    var tPCard:TPCard = null
                    fpt4.logic02Recs.foreach( sLogic02Rec =>
                      tPCard = TPFPT2ProtoBuffer(sLogic02Rec,fpt4)
                    )
                    tPCardService.addTPCard(tPCard)
                    //TODO 发查询
                    queryService.addMatchTask(null)
                  }else if(fpt4.logic03Recs.length>0){
                    // TODO 现场处理
                  }else{
                    throw new Exception("接收FPT逻辑描述记录类型不正确")
                  }
              }
              try{
                val flag:Int = searchTaskService.setSearchTaskStatus(userid, password, taskControlID, true)
//                if(flag!=1){
//                  error("call setSearchTaskStatus faild!inputParam:"+ true + " returnVal:" + flag)
//                }
              }catch{
                case e:Exception => error("setSearchTaskStatus-error:" + e.getMessage)
              }
          }catch{
              case e:Exception=> error("WsSearchTaskCronService-error:" + e.getMessage)
                try{
                  val flag:Int = searchTaskService.setSearchTaskStatus(userid, password, taskControlID, false)
//                  if(flag!=1){
//                    case e:Exception=> error("call setSearchTaskStatus faild!inputParam:"+ false + " returnVal:" + flag)
//                  }
                }catch{
                  case e:Exception=> error("setSearchTaskStatus-error:" + e.getMessage)
                }
          }
        }
      })
  }





  /**
    * 根据接口获得的FPT文件,构建TPCard的ProtoBuffer的对象
      * 在构建过程中,需要解压图片获得原图，提取特征
    * @param fpt4
    * @return
    */
  def TPFPT2ProtoBuffer(logic02Rec:Logic02Rec,fpt4:FPT4File):TPCard ={
    val tpCard = TPCard.newBuilder()
    val textBuilder = tpCard.getTextBuilder
    tpCard.setStrCardID(fpt4.sid)
    textBuilder.setStrName(logic02Rec.personName)
    textBuilder.setStrAliasName (logic02Rec.alias)
    textBuilder.setNSex(if(logic02Rec.gender==null){logic02Rec.gender.toInt}else{9})
    textBuilder.setStrBirthDate (logic02Rec.birthday)
    textBuilder.setStrIdentityNum (logic02Rec.idCardNo)
    textBuilder.setStrBirthAddrCode (logic02Rec.addressDetail)
    textBuilder.setStrBirthAddr (logic02Rec.address)
    textBuilder.setStrAddrCode (logic02Rec.doorDetail)
    textBuilder.setStrAddr (logic02Rec.door)
    textBuilder.setStrPersonType (logic02Rec.category)
    textBuilder.setStrCaseType1 (logic02Rec.caseClass1Code)
    textBuilder.setStrCaseType2 (logic02Rec.caseClass2Code)
    textBuilder.setStrCaseType3 (logic02Rec.caseClass3Code)
    textBuilder.setStrPrintUnitCode (logic02Rec.gatherUnitCode)
    textBuilder.setStrPrintUnitName (logic02Rec.gatherName)
    textBuilder.setStrPrinter (logic02Rec.gatherName)
    textBuilder.setStrPrintDate (logic02Rec.gatherDate)
    textBuilder.setStrComment (logic02Rec.remark)
    textBuilder.setStrNation (logic02Rec.nation)
    textBuilder.setStrRace (logic02Rec.nativeplace)
    textBuilder.setStrCertifType (logic02Rec.certificateType)
    textBuilder.setStrCertifID (logic02Rec.certificateNo)
    textBuilder.setBHasCriminalRecord (if(logic02Rec.isCriminal==0){logic02Rec.isCriminal == false}else{logic02Rec.isCriminal == true})
    textBuilder.setStrCriminalRecordDesc (logic02Rec.criminalInfo)
    textBuilder.setStrPremium (logic02Rec.assistUnitName)
    textBuilder.setNXieChaFlag (if(logic02Rec.isAssist==null){logic02Rec.isAssist.toInt}else{9})
    textBuilder.setStrXieChaRequestUnitName (logic02Rec.assistUnitName)
    textBuilder.setStrXieChaRequestUnitCode (logic02Rec.assistUnitCode)
    textBuilder.setNXieChaLevel (if(logic02Rec.assistLevel==null){logic02Rec.assistLevel.toInt}else{9})
    textBuilder.setStrXieChaForWhat (logic02Rec.assistPurpose)
    textBuilder.setStrRelPersonNo (logic02Rec.relatedPersonId)
    textBuilder.setStrRelCaseNo (logic02Rec.relatedCaseId)
    textBuilder.setStrXieChaTimeLimit (logic02Rec.assistTimeLimit)
    textBuilder.setStrXieChaDate (logic02Rec.assistDate)
    textBuilder.setStrXieChaRequestComment (logic02Rec.assistAskingInfo)
    textBuilder.setStrXieChaContacter (logic02Rec.contact)
    textBuilder.setStrXieChaTelNo (logic02Rec.contactPhone)
    textBuilder.setStrShenPiBy (logic02Rec.approver)


    logic02Rec.fingers.foreach { tData =>
      if (tData.imgData != null && tData.imgData.length > 0) {
        val tBuffer = FPTFileHandler.fingerDataToGafisImage(tData)
        //图像解压
        val s = FPTFileHandler.callHallImageDecompressionImage(tBuffer)
        //提取特征
        val mntAndBin = FPTFileHandler.extractorFeature(s,fgpParesEnum(tData.fgp),ParseFeatureTypeEnum(fpt4.tpCount.toInt,fpt4.lpCount.toInt))
        val blobBuilder = tpCard.addBlobBuilder()
        blobBuilder.setStMntBytes(ByteString.copyFrom(mntAndBin.get._1.toByteArray()))
        blobBuilder.setType(ImageType.IMAGETYPE_FINGER)
        blobBuilder.setFgp(fgpParesProtoBuffer(tData.fgp))
        blobBuilder.setStImageBytes(ByteString.copyFrom(s.toByteArray()))
        blobBuilder.setStBinBytes(ByteString.copyFrom(mntAndBin.get._2.toByteArray()))
      }
    }
    tpCard.build()
  }

  /**
    * 将解析出的指位翻译成系统中的枚举类型
    */
  def fgpParesEnum(fgp:String): FingerPosition ={
    fgp match {
      case "01" =>
        FingerPosition.FINGER_R_THUMB
      case "02" =>
        FingerPosition.FINGER_R_INDEX
      case "03" =>
        FingerPosition.FINGER_R_MIDDLE
      case "04" =>
        FingerPosition.FINGER_R_RING
      case "05" =>
        FingerPosition.FINGER_R_LITTLE
      case "06" =>
        FingerPosition.FINGER_L_THUMB
      case "07" =>
        FingerPosition.FINGER_L_INDEX
      case "08" =>
        FingerPosition.FINGER_L_MIDDLE
      case "09" =>
        FingerPosition.FINGER_L_RING
      case "10" =>
        FingerPosition.FINGER_L_LITTLE
      case other =>
        FingerPosition.FINGER_UNDET
    }
  }


  /**
    * 将解析出的指位翻译成系统中的枚举类型,ProtoBuffer
    */
  def fgpParesProtoBuffer(fgp:String): FingerFgp ={
    fgp match {
      case "01" =>
        FingerFgp.FINGER_R_THUMB
      case "02" =>
        FingerFgp.FINGER_R_INDEX
      case "03" =>
        FingerFgp.FINGER_R_MIDDLE
      case "04" =>
        FingerFgp.FINGER_R_RING
      case "05" =>
        FingerFgp.FINGER_R_LITTLE
      case "06" =>
        FingerFgp.FINGER_L_THUMB
      case "07" =>
        FingerFgp.FINGER_L_INDEX
      case "08" =>
        FingerFgp.FINGER_L_MIDDLE
      case "09" =>
        FingerFgp.FINGER_L_RING
      case "10" =>
        FingerFgp.FINGER_L_LITTLE
      case other =>
        FingerFgp.FINGER_UNDET
    }
  }

  /**
    *
    * @param tpCount
    * @param lpCount
    * @return
    */
  def ParseFeatureTypeEnum(tpCount:Integer,lpCount:Integer): FeatureType ={
    var featureType:FeatureType = null
    if(tpCount>0){
      featureType = FeatureType.FingerTemplate
    }else if(lpCount>0){
      featureType = FeatureType.FingerLatent
    }
    featureType
  }
}
