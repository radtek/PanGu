package nirvana.hall.webservice.internal.haixin


import java.io.File
import java.text.SimpleDateFormat
import java.util
import java.util.{Date, UUID}
import javax.activation.DataHandler
import javax.sql.DataSource

import com.google.protobuf.ByteString
import monad.support.services.{LoggerSupport, XmlLoader}
import nirvana.hall.api.HallApiConstants
import nirvana.hall.api.internal.{ExceptionUtil, JniLoaderUtil}
import nirvana.hall.api.services.{ExportRelationService, QueryService, TPCardService}
import nirvana.hall.api.services.fpt.FPTService
import nirvana.hall.api.services.remote.HallImageRemoteService
import nirvana.hall.c.services.gloclib.glocdef.GAFISIMAGESTRUCT
import nirvana.hall.extractor.services.FeatureExtractor
import nirvana.hall.image.internal.FPTImageConverter
import nirvana.hall.protocol.api.FPTProto.{ImageType, PalmFgp, TPCard}
import nirvana.hall.protocol.extract.ExtractProto.ExtractRequest.FeatureType
import nirvana.hall.protocol.extract.ExtractProto.FingerPosition
import nirvana.hall.support.services.JdbcDatabase
import nirvana.hall.webservice.config.HallWebserviceConfig
import nirvana.hall.webservice.internal.haixin.vo._
import nirvana.hall.webservice.services.haixin.{StrategyService, WsHaiXinFingerService}
import org.apache.axiom.attachments.ByteArrayDataSource
import org.apache.commons.io.{FileUtils, IOUtils}

/**
  * Created by yuchen on 2017/7/24.
  */
class WsHaiXinFingerServiceImpl(implicit dataSource: DataSource
                                ,hallImageRemoteService: HallImageRemoteService
                                ,strategyService:StrategyService
                                ,fptService: FPTService
                                ,tpCardService: TPCardService
                                ,queryService: QueryService
                                ,extractor: FeatureExtractor
                                ,exportRelationService:ExportRelationService
                                ,hallWebserviceConfig: HallWebserviceConfig) extends WsHaiXinFingerService with LoggerSupport{
  JniLoaderUtil.loadExtractorJNI()
  JniLoaderUtil.loadImageJNI()
  /**
    * 接口01:捺印指纹信息录入
    *
    * @param collectsrc 来源
    * @param userid     用户id
    * @param unitcode   单位代码
    * @param personid   公安部标准的23位唯一码，人员编号
    * @param dh         FPT载体
    * @return 1: 成功加入队列 0: 失败
    */
  override def setFinger(collectsrc: String, userid: String, unitcode: String, personid: String, dh: DataHandler): Int = {

    var result = IAConstant.ADD_QUEUE_FAIL
    val uuid = UUID.randomUUID().toString.replace("-",IAConstant.EMPTY)
    var queryId :Option[String]= None
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("collectsrc",collectsrc)
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("personid",personid)
      paramMap.put("dh",dh)

      strategyService.inputParamIsNullOrEmpty(paramMap)

      if(hallWebserviceConfig.saveFPTFlag.equals("1")){
        FileUtils.writeByteArrayToFile(new File(hallWebserviceConfig.localTenprintPath + personid + ".FPT"), IOUtils.toByteArray(dh.getInputStream))
      }
      strategyService.checkCollectSrcIsVaild(collectsrc)
      strategyService.checkUserIsVaild(userid,unitcode)
      strategyService.checkFingerCardIsExist(personid,IAConstant.SET_FINGER)
      val fpt = strategyService.checkFptIsVaild(personid,dh)
      queryId = Some(fpt.sid)
      var cardId = ""
      fpt.logic02Recs.foreach{
        t =>
          cardId = t.personId.toUpperCase
          if(cardId.startsWith("R")) t.cardId = cardId.drop(1) else t.cardId = cardId
          fptService.addLogic02Res(t,Some(hallWebserviceConfig.templateFingerDBId))
      }
      strategyService.fingerBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
        ,IAConstant.ADD_QUEUE_SUCCESS
        ,IAConstant.SET_FINGER,personid,queryId.getOrElse(IAConstant.EMPTY_ORASID.toString),dh,None)

      result = IAConstant.ADD_QUEUE_SUCCESS

    }catch{
      case ex:Throwable =>
        strategyService.fingerBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
          ,IAConstant.ADD_QUEUE_FAIL
          ,IAConstant.SET_FINGER,personid,queryId.getOrElse(IAConstant.EMPTY_ORASID.toString),dh,Some(ex))
        result = IAConstant.ADD_QUEUE_FAIL
    }
    result
  }

  /**
    * 接口02:查询捺印指纹在指纹系统中的状态
    *
    * @param userid   用户id
    * @param unitcode 单位代码
    * @param personid 公安部标准的23位唯一码，人员编号
    * @return 4: 建库失败 3: 处理中 2: 已比中 1: 成功建库 0: 查询失败
    */
  override def getFingerStatus(userid: String, unitcode: String, personid: String): Int = {
    /**
      *指纹业务表与发查询表左联查，取出response_status，orasid
      * if(response_status == 0){
      *    =>4: 建库失败
      * }
      * else if(response_status = 1 && orasid != null){
      *   用orasid调用hall6.2接口，获得状态
      *   3: 处理中
          2: 已比中
      * }else{
      *   =>成功建库
      * }
      * 抛异常时，=>查询失败
      */
    var result = IAConstant.QUERY_FAIL
    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("personid",personid)
      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkUserIsVaild(userid,unitcode)
      strategyService.checkFingerCardIsExist(personid,IAConstant.GET_FINGER_STATUS)
      val cardId = if(personid.toUpperCase.startsWith("R")) personid.toUpperCase.drop(1) else personid.toUpperCase
      val responseStatusAndOraSidMap = strategyService.getRemoteResponseStatusAndOrasidByPersonId(cardId)
      responseStatusAndOraSidMap match{
        case Some(t) =>
          val oraSid = t("orasid").asInstanceOf[Long].toLong
          if(oraSid > 0){
            val status = getStatusBySidSQL(oraSid)
            result = strategyService.getResponseStatusByGafisStatus_TT(status)
          }else{
            result = IAConstant.CREATE_STORE_SUCCESS
          }
          //tpCardService.delTPCard(cardId,Some(hallWebserviceConfig.templateFingerDBId))
        case _ =>
          if(tpCardService.isExist(cardId,Some(hallWebserviceConfig.templateFingerDBId))){
            result = IAConstant.CREATE_STORE_SUCCESS
          }else{
            result = IAConstant.CREATE_STORE_FAIL
          }
      }
      strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
        ,result,IAConstant.GET_FINGER_STATUS,personid,IAConstant.EMPTY_ORASID.toString,null,None)
    }catch{
      case ex:Throwable =>
        strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
          ,IAConstant.QUERY_FAIL
          ,IAConstant.GET_FINGER_STATUS,personid,IAConstant.EMPTY_ORASID.toString,null,Some(ex))
        result = IAConstant.QUERY_FAIL
    }
    result
  }

  /**
    * 接口03:捺印指纹信息更新
    *
    * @param collectsrc 来源
    * @param userid     用户id
    * @param unitcode   单位代码
    * @param personid   公安部标准的23位唯一码，人员编号
    * @param dh         FPT载体
    * @return 1: 成功加入队列 0: 失败
    */
  override def setFingerAgain(collectsrc: String, userid: String, unitcode: String, personid: String, dh: DataHandler): Int = {
    var result = IAConstant.ADD_QUEUE_FAIL
    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("collectsrc",collectsrc)
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("personid",personid)
      paramMap.put("dh",dh)

      strategyService.inputParamIsNullOrEmpty(paramMap)

      if(hallWebserviceConfig.saveFPTFlag.equals("1")){
        FileUtils.writeByteArrayToFile(new File(hallWebserviceConfig.localTenprintPath + personid + "update.FPT"), IOUtils.toByteArray(dh.getInputStream))
      }

      strategyService.checkCollectSrcIsVaild(collectsrc)
      strategyService.checkUserIsVaild(userid,unitcode)
      strategyService.checkFingerCardIsExist(personid,IAConstant.SET_FINGER_AGAIN)
      val fpt = strategyService.checkFptIsVaild(personid,dh)
      var cardId = ""
      fpt.logic02Recs.foreach{
        t =>
          cardId = t.personId.toUpperCase
          if(cardId.startsWith("R")) t.cardId = cardId.drop(1) else t.cardId = cardId
          fptService.updateLogic02Res(t,Some(hallWebserviceConfig.templateFingerDBId))
      }
      strategyService.fingerBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
        ,IAConstant.ADD_QUEUE_SUCCESS
        ,IAConstant.SET_FINGER_AGAIN,personid,IAConstant.EMPTY_ORASID.toString,dh,None)
      result = IAConstant.ADD_QUEUE_SUCCESS

    }catch{

      case ex:Throwable =>
        strategyService.fingerBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
          ,IAConstant.ADD_QUEUE_FAIL
          ,IAConstant.SET_FINGER_AGAIN,personid,IAConstant.EMPTY_ORASID.toString,dh,Some(ex))
        result = IAConstant.ADD_QUEUE_FAIL
    }
    result
  }

  /**
    * 接口04:获取捺印指纹的比中列表
    *
    * @param userid   用户id
    * @param unitcode 单位代码
    * @param timefrom 起始时间(YYYY/MM/DD HH24:MI:SS) 选填
    * @param timeto   结束时间(YYYY/MM/DD HH24:MI:SS) 选填
    * @param Startnum 记录起始位置编号(默认为1)
    * @param endnum   记录结束位置编号(默认为10）
    * @return XML文件 无信息或异常返回空
    */
  override def getFingerMatchList(userid: String, unitcode: String, timefrom: String, timeto: String, Startnum: Int, endnum: Int): DataHandler = {

    var dataHandler:DataHandler = null

    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)

      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkUserIsVaild(userid,unitcode)

      val listBuffer = strategyService.getHitList(timefrom,timeto,Startnum,endnum)

      listBuffer match {
        case Some(m) =>

          dataHandler = getXMLFile(m)

          strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY
            ,userid,unitcode
            ,IAConstant.SEARCH_SUCCESS
            ,IAConstant.GET_FINGER_MATCH_LIST
            ,IAConstant.EMPTY,IAConstant.EMPTY_ORASID.toString,dataHandler,None)

        case _ =>
      }

    }catch{
      case ex:Throwable =>
        strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
          ,IAConstant.SEARCH_FAIL,IAConstant.GET_FINGER_MATCH_LIST,IAConstant.EMPTY,IAConstant.EMPTY_ORASID.toString,null,Some(ex))
    }
    dataHandler
  }


  /**
    * 接口05:获取捺印指纹的比中数量
    *
    * @param userid   用户id
    * @param unitcode 单位代码
    * @param timefrom 起始时间(YYYY/MM/DD HH24:MI:SS) 选填
    * @param timeto   结束时间(YYYY/MM/DD HH24:MI:SS) 选填
    * @return n（n>=0）:已录入数据比中个数；（n<0）: 参数或接口异常返回。
    */
  override def getFingerMatchCount(userid: String, unitcode: String, timefrom: String, timeto: String): Int = {

    var count = 0
    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)

      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkUserIsVaild(userid,unitcode)

      count = strategyService.getHitCount(timefrom,timeto)
      logger.info("get Hit count|userid:" + userid
        + "&unitcode:" + unitcode
        + "&timefrom:" + timefrom
        + "&timeto:" + timeto
        + "&count:" + count)

      strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY
        ,userid,unitcode,count
        ,IAConstant.GET_FINGER_MATCH_COUNT
        ,IAConstant.EMPTY,IAConstant.EMPTY_ORASID.toString,null,None)

    }catch{

      case ex:Throwable =>
        strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
          ,IAConstant.SEARCH_FAIL,IAConstant.GET_FINGER_MATCH_COUNT,IAConstant.EMPTY,IAConstant.EMPTY_ORASID.toString,null,Some(ex))
    }
    count
  }

  /**
    * 接口06:根据请求方指纹编号(人员编号)获取捺印指纹的比中信息
    *
    * @param userid   用户id
    * @param unitcode 单位代码
    * @param personid 公安部标准的23位唯一码，人员编号
    * @return DataHandler 比中关系的FPT文件
    */
  override def getFingerMatchData(userid: String, unitcode: String, personid: String): util.ArrayList[DataHandler] = {

    val listDataHandler = new util.ArrayList[DataHandler]

    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("personid",personid)

      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkUserIsVaild(userid,unitcode)
      strategyService.checkFingerCardIsExist(personid,IAConstant.GET_FINGER_MATCH_DATA)

      val listMapBuffer = strategyService.getOraSidAndQueryIdByPersonId(if(personid.toUpperCase.startsWith("R")) personid.toUpperCase.drop(1) else personid.toUpperCase)

      listMapBuffer match {
        case Some(m) => m.foreach{ t =>

          val dataHandler = exportRelationService.exportMatchRelation(t.get("queryid").get.toString
            ,t.get("orasid").get.toString)
          if(Nil != dataHandler && null != dataHandler){
            listDataHandler.add(dataHandler)
          }
        }
        case _ => throw new Exception("not found orasid,personid:" + personid)
      }

      saveHitResultFPT(listDataHandler,personid)

      strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY
        ,userid,unitcode,IAConstant.SEARCH_SUCCESS
        ,IAConstant.GET_FINGER_MATCH_DATA
        ,personid,IAConstant.EMPTY_ORASID.toString,null,None)


    }catch{

      case ex:Throwable =>

      strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
        ,IAConstant.SEARCH_FAIL,IAConstant.GET_FINGER_MATCH_DATA,personid,IAConstant.EMPTY_ORASID.toString,null,Some(ex))
    }
    listDataHandler
  }

  /**
    * 接口07:获取指纹系统服务器时间
    *
    * @return String 指纹系统服务器时间 (YYYY/MM/DD HH24:MI:SS),接口异常返回空。
    */
  override def getSysTime(): String = {
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date)
  }


  private def getXMLFile(listItem:util.ArrayList[String]): DataHandler ={

    val list = new ListItem
    list.Item = listItem
    list.itemCnt = listItem.size
    val hitConfig = new HitConfig
    hitConfig.List = list

    new DataHandler(new ByteArrayDataSource(XmlLoader.toXml(hitConfig).getBytes))
  }

  /**
    * 接口08:捺印掌纹信息录入
    *
    * @param collectsrc 来源
    * @param userid     用户id
    * @param unitcode   单位代码
    * @param palmid     请求方掌纹编号
    * @param palmtype   公安部掌纹部位代码
    * @param personid   公安部标准的23位唯一码，人员编号
    * @param dh         WSQ文件
    * @return 1: 成功加入队列 0: 失败
    */
  override def setPalm(collectsrc: String, userid: String, unitcode: String, palmid: String, palmtype: Int, personid: String, dh: Array[Byte]): Int = {

    var result = IAConstant.ADD_QUEUE_FAIL
    val uuid = UUID.randomUUID().toString.replace("-",IAConstant.EMPTY)
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("collectsrc",collectsrc)
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("palmid",palmid)
      paramMap.put("palmtype",palmtype)
      paramMap.put("personid",personid)
      paramMap.put("dh",dh)

      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkCollectSrcIsVaild(collectsrc)
      strategyService.checkUserIsVaild(userid,unitcode)
      strategyService.checkPalmIsExist(palmid,palmtype,IAConstant.SET_PALM)

      addPalmTpCard(personid,dh,palmtype)

      strategyService.palmBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
        ,IAConstant.ADD_QUEUE_SUCCESS
        ,IAConstant.SET_PALM,palmid,palmtype,personid,dh,None)

      result = IAConstant.ADD_QUEUE_SUCCESS

    }catch{
      case ex:Throwable =>
        strategyService.palmBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
          ,IAConstant.ADD_QUEUE_FAIL
          ,IAConstant.SET_PALM,palmid,palmtype,personid,dh,Some(ex))
        result = IAConstant.ADD_QUEUE_FAIL
    }
    result
  }

  /**
    * 接口09:捺印掌纹信息状态查询
    *
    * @param userid   用户id
    * @param unitcode 单位代码
    * @param palmid   请求方掌纹编号
    * @param palmtype 公安部掌纹部位代码
    * @return 4: 建库失败 3: 处理中 1: 成功建库 0: 查询失败
    */
  override def getPalmStatus(userid: String, unitcode: String, palmid: String, palmtype: Int): Int = {
    var result = IAConstant.QUERY_FAIL
    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("palmid",palmid)
      paramMap.put("palmtype",palmtype)
      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkUserIsVaild(userid,unitcode)
      strategyService.checkPalmIsExist(palmid,palmtype,IAConstant.GET_PALM_STATUS)
      val count = strategyService.getResponseStatusAndPlam(palmid,palmtype)
      if(count<0){
        result = IAConstant.CREATE_STORE_FAIL
      }else{
        result = IAConstant.CREATE_STORE_SUCCESS
      }
      strategyService.palmBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
        ,result,IAConstant.GET_PALM_STATUS,palmid,palmtype,IAConstant.EMPTY,null,None)
    }catch{
      case ex:Throwable =>
        strategyService.palmBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
          ,IAConstant.QUERY_FAIL
          ,IAConstant.GET_PALM_STATUS,palmid,palmtype,IAConstant.EMPTY,null,Some(ex))
        result = IAConstant.QUERY_FAIL
    }
    result
  }

  /**
    * 接口10:捺印掌纹信息更新
    *
    * @param collectsrc 来源
    * @param userid     用户id
    * @param unitcode   单位代码
    * @param palmid     请求方掌纹编号
    * @param palmtype   公安部掌纹部位代码
    * @param personid   公安部标准的23位唯一码，人员编号
    * @param dh         WSQ文件
    * @return 1: 成功加入队列 0: 失败
    */
  override def setPalmAgain(collectsrc: String, userid: String, unitcode: String, palmid: String, palmtype: Int, personid: String, dh: Array[Byte]): Int = {

    var result = IAConstant.ADD_QUEUE_FAIL
    val uuid = UUID.randomUUID().toString.replace("-",IAConstant.EMPTY)
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("collectsrc",collectsrc)
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("palmid",palmid)
      paramMap.put("palmtype",palmtype)
      paramMap.put("personid",personid)
      paramMap.put("dh",dh)

      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkCollectSrcIsVaild(collectsrc)
      strategyService.checkUserIsVaild(userid,unitcode)
      strategyService.checkPalmIsExist(palmid,palmtype,IAConstant.SET_PALM_AGAIN)

      addPalmTpCard(personid,dh,palmtype)

      strategyService.palmBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
        ,IAConstant.ADD_QUEUE_SUCCESS
        ,IAConstant.SET_PALM_AGAIN,palmid,palmtype,personid,dh,None)

      result = IAConstant.ADD_QUEUE_SUCCESS

    }catch{
      case ex:Throwable =>
        strategyService.palmBusinessFinishedHandler(uuid,collectsrc,userid,unitcode
          ,IAConstant.ADD_QUEUE_FAIL
          ,IAConstant.SET_PALM_AGAIN,palmid,palmtype,personid,dh,Some(ex))
        result = IAConstant.ADD_QUEUE_FAIL
    }
    result
  }

  private def addPalmTpCard(personid:String,palmImage:Array[Byte],palmType:Int): Unit ={
    var tpCard = TPCard.newBuilder()
    if(tpCardService.isExist(personid)){
      tpCard = tpCardService.getTPCard(personid).toBuilder
      val iter = tpCard.getBlobBuilderList.iterator()
      while (iter.hasNext){
        val blob = iter.next()
        blob.getType match {
          case ImageType.IMAGETYPE_PALM =>
            blob.setStImageBytes(ByteString.copyFrom(FPTImageConverter.convert2GafisPalmImage(palmImage,palmType).toByteArray()))
          case _ =>
        }
      }
      tpCardService.updateTPCard(tpCard.build())
    }else{
      val textBuilder = tpCard.getTextBuilder
      tpCard.setStrCardID(if(personid.toUpperCase.startsWith(HallApiConstants.TPCARDNO_HEAD_LETTER)) personid.drop(1) else personid)
      tpCard.setStrPersonID(personid)
      textBuilder.setStrName("")
      textBuilder.setStrAliasName("")
      textBuilder.setNSex(0)
      textBuilder.setStrBirthDate("")
      textBuilder.setStrIdentityNum("")
      textBuilder.setStrBirthAddrCode("")
      textBuilder.setStrBirthAddr("")
      textBuilder.setStrAddrCode("")
      textBuilder.setStrAddr("")
      textBuilder.setStrPersonType("")
      textBuilder.setStrCaseType1("")
      textBuilder.setStrCaseType2("")
      textBuilder.setStrCaseType3("")
      textBuilder.setStrPrintUnitCode("")
      textBuilder.setStrPrintUnitName("")
      textBuilder.setStrPrinter("")
      textBuilder.setStrPrintDate("")
      textBuilder.setStrComment("")
      textBuilder.setStrNation("")
      textBuilder.setStrRace("")
      textBuilder.setStrCertifType("")
      textBuilder.setStrCertifID("")
      textBuilder.setBHasCriminalRecord(false)
      textBuilder.setStrCriminalRecordDesc("")
      textBuilder.setStrPremium("")
      textBuilder.setNXieChaFlag(0)
      textBuilder.setStrXieChaRequestUnitName("")
      textBuilder.setStrXieChaRequestUnitCode("")
      textBuilder.setNXieChaLevel(0)
      textBuilder.setStrXieChaForWhat("")
      textBuilder.setStrRelPersonNo("")
      textBuilder.setStrRelCaseNo("")
      textBuilder.setStrXieChaTimeLimit("")
      textBuilder.setStrXieChaDate("")
      textBuilder.setStrXieChaRequestComment("")
      textBuilder.setStrXieChaContacter("")
      textBuilder.setStrXieChaTelNo("")
      textBuilder.setStrShenPiBy("")

      val originalImage = hallImageRemoteService.decodeGafisImage(FPTImageConverter.convert2GafisPalmImage(palmImage,palmType))
      val mntData = extractByGAFISIMG(originalImage, false)
      val blobBuilder = tpCard.addBlobBuilder
      blobBuilder.setStMntBytes(ByteString.copyFrom(mntData._1.toByteArray()))
      blobBuilder.setPalmfgp(fgpParesProtoBuffer(palmType.toString))
      blobBuilder.setStBinBytes(ByteString.copyFrom(mntData._2.toByteArray()))
      blobBuilder.setStImageBytes(ByteString.copyFrom(FPTImageConverter.convert2GafisPalmImage(palmImage,palmType).toByteArray()))
      blobBuilder.setType(ImageType.IMAGETYPE_PALM)

      tpCardService.addTPCard(tpCard.build())
    }
  }

  private def extractByGAFISIMG(originalImage: GAFISIMAGESTRUCT, isLatent: Boolean): (GAFISIMAGESTRUCT, GAFISIMAGESTRUCT) ={
    if(isLatent){
      extractor.extractByGAFISIMG(originalImage, FingerPosition.FINGER_UNDET, FeatureType.FingerLatent)
    }else{
      val fingerIndex = originalImage.stHead.nFingerIndex
      val fingerPos = if(fingerIndex > 10){
        FingerPosition.valueOf(fingerIndex - 10)
      }else{
        FingerPosition.valueOf(fingerIndex)
      }
      extractor.extractByGAFISIMG(originalImage, fingerPos, FeatureType.FingerTemplate)
    }
  }


  /**
    * 将解析出的指位翻译成系统中的枚举类型,ProtoBuffer
    */
  def fgpParesProtoBuffer(fgp:String): PalmFgp ={
    fgp match {
      case "1" =>
        PalmFgp.PALM_RIGHT
      case "2" =>
        PalmFgp.PALM_LEFT
      case "3" =>
        PalmFgp.PALM_FINGER_R
      case "4" =>
        PalmFgp.PALM_FINGER_L
      case "5" =>
        PalmFgp.PALM_THUMB_R_LOW
      case "6" =>
        PalmFgp.PALM_THUMB_R_UP
      case "7" =>
        PalmFgp.PALM_THUMB_L_LOW
      case "8" =>
        PalmFgp.PALM_THUMB_L_UP
      case "9" =>
        PalmFgp.PALM_RIGTH_SIDE
      case "10" =>
        PalmFgp.PALM_LEFT_SIDE
      case other =>
        PalmFgp.PALM_UNKNOWN
    }
  }


  private def saveHitResultFPT(listDataHandler:util.ArrayList[DataHandler],personid:String): Unit ={
    try{
      if(hallWebserviceConfig.saveFPTFlag.equals("1")){
        val count = listDataHandler.size
        for( i <- 0 until count){
          FileUtils.writeByteArrayToFile(new File(hallWebserviceConfig.localHitResultPath
                         + personid + "\\" + personid + "_" +i + ".FPT")
            , IOUtils.toByteArray(listDataHandler.get(i).getInputStream))
        }
      }
    }catch{
      case e:Exception => logger.error("export hit result fail:" + ExceptionUtil.getStackTraceInfo(e))
    }
  }
  /**
    * 根据任务号sid获取比对状态 SQL查询方式
    *
    * @param oraSid
    */
  private def getStatusBySidSQL(oraSid: Long): Int = {
    val sql = s"select t.status from NORMALQUERY_QUERYQUE t where t.ora_sid = ?"
    var status = 0
    JdbcDatabase.queryWithPsSetter(sql) { ps =>
      ps.setInt(1,oraSid.toInt)
    } { rs =>
      status = rs.getInt("status")
    }
    status
  }

  /**
    * 接口11:获取错误详细信息
    * @param userid 用户id
    * @param unitcode 单位代码
    * @param funType 接口方法类型
    * @param personid 公安部标准的23位唯一码，人员编号
    * @return
    */
  override def getErrorInfo(userid:String,unitcode:String,funType:Int,personid : String) : DataHandler = {
    var dataHandler:DataHandler = null

    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("funType",funType)
      paramMap.put("personid",personid)

      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkUserIsVaild(userid,unitcode)

      val listBuffer = strategyService.getErrorInfoList(userid,unitcode,personid,funType)
      val errorInfoList = new util.ArrayList[ErrorInfoItem]
      if(listBuffer.length > 0){
        listBuffer.foreach{
          m =>
            val errorInfoItem = new ErrorInfoItem
            errorInfoItem.time = m.get("time").get.toString
            errorInfoItem.exception = m.get("exception").get.toString
            errorInfoList.add(errorInfoItem)
        }
      }
      val errorInfo = new ErrorInfo
      errorInfo.Item = errorInfoList
      dataHandler = new DataHandler(new ByteArrayDataSource(XmlLoader.toXml(errorInfo).getBytes))
      strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY
        ,userid,unitcode
        ,IAConstant.SEARCH_SUCCESS
        ,IAConstant.GET_ERROR_INFO
        ,personid,IAConstant.EMPTY_ORASID.toString,null,None)
    }catch{
      case ex:Throwable =>
        strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
          ,IAConstant.SEARCH_FAIL,IAConstant.GET_ERROR_INFO,personid,IAConstant.EMPTY_ORASID.toString,null,Some(ex))
    }
    dataHandler
  }

  /**
    * 接口12：根据身份证号获取人员信息
    *
    * @param userid
    * @param unitcode
    * @param idcard
    * @return
    */
  override def getPersonInfo(userid: String, unitcode: String, idcard: String) : DataHandler = {
    var dataHandler:DataHandler = null

    val uuid = UUID.randomUUID().toString.replace("-","")
    try{
      val paramMap = new scala.collection.mutable.HashMap[String,Any]
      paramMap.put("userid",userid)
      paramMap.put("unitcode",unitcode)
      paramMap.put("idcard",idcard)

      strategyService.inputParamIsNullOrEmpty(paramMap)
      strategyService.checkUserIsVaild(userid,unitcode)

      val personInfoList = strategyService.getPersonInfo(idcard);
      val personInfo = new PersonInfo
      personInfo.Item = personInfoList
      dataHandler = new DataHandler(new ByteArrayDataSource(XmlLoader.toXml(personInfo).getBytes))
      strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY
        ,userid,unitcode
        ,IAConstant.SEARCH_SUCCESS
        ,IAConstant.GET_PERSON_INFO
        ,idcard,IAConstant.EMPTY_ORASID.toString,null,None)
    }catch{
      case ex:Throwable =>
        strategyService.fingerBusinessFinishedHandler(uuid,IAConstant.EMPTY,userid,unitcode
          ,IAConstant.SEARCH_FAIL,IAConstant.GET_PERSON_INFO,idcard,IAConstant.EMPTY_ORASID.toString,null,Some(ex))
    }
    dataHandler
  }
}
