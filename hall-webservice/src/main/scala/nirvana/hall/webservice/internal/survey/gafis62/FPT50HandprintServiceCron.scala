package nirvana.hall.webservice.internal.survey.gafis62

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, UUID}

import monad.core.services.{CronScheduleWithStartModel, StartAtDelay}
import monad.support.services.LoggerSupport
import nirvana.hall.api.internal.{DataConverter, DateConverter, ExceptionUtil}
import nirvana.hall.api.services.MatchRelationService
import nirvana.hall.api.services.fpt.FPT5Service
import nirvana.hall.c.services.gloclib.survey
import nirvana.hall.c.services.gloclib.survey.{SURVEYCONFIG, SURVEYRECORD}
import nirvana.hall.v62.internal.V62Facade
import nirvana.hall.webservice.CronExpParser
import nirvana.hall.webservice.config.{HallWebserviceConfig, SurveyConfig}
import nirvana.hall.webservice.internal.survey.{PlatformOperatorInfoProvider, PlatformOperatorInfoProviderLoader, SurveyConstant, SurveyFatal}
import nirvana.hall.webservice.jpa.survey.LogInterfacestatus
import nirvana.hall.webservice.services.survey.{SurveyConfigService, SurveyHitResultRecordService, SurveyRecordService}
import org.apache.commons.lang.StringUtils
import org.apache.tapestry5.ioc.annotations.PostInjection
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor

/**
  * Created by songpeng on 2017/12/24.
  * 现场勘验系统接口定时服务
  */
class FPT50HandprintServiceCron(hallWebserviceConfig: HallWebserviceConfig,
                                surveyConfigService: SurveyConfigService,
                                surveyRecordService: SurveyRecordService,
                                surveyHitResultRecordService: SurveyHitResultRecordService,
                                matchRelationService: MatchRelationService,
                                fPT5Service: FPT5Service) extends LoggerSupport {

  val fPT50HandprintServiceClient = new FPT50HandprintServiceClient(hallWebserviceConfig.handprintService)
  var provider: Option[PlatformOperatorInfoProvider] = None
  if (hallWebserviceConfig.handprintService.platformOperatorInfoProviderClass != null) {
    provider = Option(PlatformOperatorInfoProviderLoader.createProvider(hallWebserviceConfig.handprintService.platformOperatorInfoProviderClass))
  }
  createSurveyConfig()


  @PostInjection
  def startUp(periodicExecutor: PeriodicExecutor): Unit = {

    if (hallWebserviceConfig.handprintService.cron != null) {
      //程序执行创建config表信息
      createSurveyConfig()
      //获取现场指纹列表定时
      periodicExecutor.addJob(new CronScheduleWithStartModel(hallWebserviceConfig.handprintService.cron, StartAtDelay), "survey-cron-getLatentList", new Runnable {
        override def run(): Unit = {
          try {
            info("begin getLatentList")
            //检查金指获取列表服务
            checkJinZhiGetLatentListService
            //根据配置里的62应用服务器地址，使用动态变量访问
            if (hallWebserviceConfig.handprintService.surveyV62ServiceConfig != null) {
              hallWebserviceConfig.handprintService.surveyV62ServiceConfig.foreach { surveyV62ServiceConfig =>
                V62Facade.withConfigurationServer(surveyV62ServiceConfig.v62ServerConfig) {
                  //initSurveyConfig(surveyV62ServiceConfig.surveyConfig)
                  getLatentList(surveyV62ServiceConfig.surveyConfig)
                }
              }
            }
            info("end  getLatentList")
          } catch {
            case ex: Exception =>
              error("getLatentList-error:{},currentTime:{}"
                , ExceptionUtil.getStackTraceInfo(ex), DateConverter.convertDate2String(new Date, SurveyConstant.DATETIME_FORMAT)
              )
          }
        }
      })
    }
  }

  /**
    * 初始化配置,如果本地有对应的单位代码则跳过，否则新增SurveyConfig配置
    *
    * @param surveyConfigList SurveyConfig配置列表
    */
  def initSurveyConfig(surveyConfigList: Seq[SurveyConfig]): Unit = {
    try {
      if (surveyConfigList != null) {
        info("xmlsurveyConfigList列表信息{}", surveyConfigList.length)
        val surveyConfigList2 = surveyConfigService.getSurveyConfigList()
        info("6.2surveyconfig表记录信息大小{}", surveyConfigList2.length)
        surveyConfigList.foreach { surveyConfig =>
//          if(surveyConfigList2.nonEmpty){
//            val count = surveyConfigList2.filter(_.szUnitCode == surveyConfig.unitCode).length
//            if (count == 0) {
//              val config = new SURVEYCONFIG
//              config.szUnitCode = surveyConfig.unitCode
//              config.szStartTime = surveyConfig.startTime
//              config.szEndTime = surveyConfig.endTime
//              config.nSeq = 1
//              config.nFlages = 1 //启用
//              config.szConfig = surveyConfig.config
//              info("添加信息到6.2应用服务器surveyConfig_unitCode{}", surveyConfig.unitCode)
//              surveyConfigService.addSurveyConfig(config)
//              info("添加完6.2应用服务器surveyConfig_unitCode{}", surveyConfig.unitCode)
//            }
//          }else{
//            val config = new SURVEYCONFIG
//            config.szUnitCode = surveyConfig.unitCode
//            config.szStartTime = surveyConfig.startTime
//            config.szEndTime = surveyConfig.endTime
//            config.nSeq = 1
//            config.nFlages = 1 //启用
//            config.szConfig = surveyConfig.config
//            info("添加信息到6.2应用服务器surveyConfig_unitCode{}", surveyConfig.unitCode)
//            surveyConfigService.addSurveyConfig(config)
//            info("添加完6.2应用服务器surveyConfig_unitCode{}", surveyConfig.unitCode)
//          }
            if(!surveyConfigList2.exists(p=>
              surveyConfig.unitCode.equals(p.szUnitCode))){
              val config = new SURVEYCONFIG
              config.szUnitCode = surveyConfig.unitCode
              config.szStartTime = surveyConfig.startTime
              config.szEndTime = surveyConfig.endTime
              config.nSeq = 1
              config.nFlages = 1 //启用
              config.szConfig = surveyConfig.config
              surveyConfigService.addSurveyConfig(config)
            }
        }
      }
    } catch {
      case ex: Exception =>
        error("initSurveyConfig-error:{},currentTime:{}"
          , ExceptionUtil.getStackTraceInfo(ex), DateConverter.convertDate2String(new Date, SurveyConstant.DATETIME_FORMAT)
        )
    }
  }

  /**
    * 获取现场指纹列表
    */
  def getLatentList(surveyConfigList: Seq[SurveyConfig]): Unit = {
    //TODO 添加日志，异常判断和处理，完善业务逻辑,目前是完全理想状态下的代码逻辑
    info("daxiao{}", surveyConfigList.length)
    try {
      //获取系统时间
      val systemDateTime = fPT50HandprintServiceClient.getSystemDateTime()
      info("系统时间{}", systemDateTime)
      //获取配置信息列表，并循环获取数据
      info("获取配置信息列表大小{}", surveyConfigService.getSurveyConfigList().filter(_.nFlages == 1).length)
      surveyConfigService.getSurveyConfigList().filter(_.nFlages == 1).foreach {
        surveyConfig =>
          info("配置信息sid{},unitcode{}", DataConverter.convertSixByteArrayToLong(surveyConfig.nSID).toString, surveyConfig.szUnitCode)
      }
      //surveyConfigService.getSurveyConfigList().filter(_.nFlages == 1).foreach(getLatentListBySurveyConfig)
      surveyConfigService.getSurveyConfigList().filter(_.nFlages == 1).foreach {
        surveyConfig =>
          if (surveyConfigList.filter(_.unitCode == surveyConfig.szUnitCode).length > 0) {
            getLatentListBySurveyConfig(surveyConfig)
          }
      }
    } catch {
      case ex: Exception =>
        error("getLatentList-error:{},currentTime:{}"
          , ExceptionUtil.getStackTraceInfo(ex), DateConverter.convertDate2String(new Date, SurveyConstant.DATETIME_FORMAT)
        )
    }
  }

  def getLatentListBySurveyConfig(surveyConfig: SURVEYCONFIG): Unit = {
    val systemDateTime = fPT50HandprintServiceClient.getSystemDateTime()
    info("海鑫系统时间{}, 获取单位代码{}现勘数据", systemDateTime, surveyConfig.szUnitCode)
    val kssj = surveyConfig.szStartTime
    var jssj = surveyConfig.szEndTime
    //如果结束时间为空，设定当前系统时间为结束时间
    if ("".equals(jssj)) {
      jssj = systemDateTime
    }
    //获取待发送现场指掌纹数量
    val latentCount = fPT50HandprintServiceClient.getLatentCount(surveyConfig.szUnitCode, hallWebserviceConfig.handprintService.dataType, "", minusDateTime(kssj), jssj)
    info("latentCount number:{}", latentCount)
    if (latentCount > 0) {
      val fingerPrintListResponse = fPT50HandprintServiceClient.getLatentList(surveyConfig.szUnitCode, hallWebserviceConfig.handprintService.dataType, "", minusDateTime(kssj), jssj, 1, latentCount)
      if (fingerPrintListResponse.nonEmpty) {
        info("单位代码:{} 现场列表大小:{}", surveyConfig.szUnitCode, fingerPrintListResponse.get.list.length)
        try {
          fingerPrintListResponse.get.list.foreach { k =>
            val xckybh = k.xckybh
            //根据现堪编号获取接警编号
            val receptionNo = fPT50HandprintServiceClient.getReceptionNo(xckybh)
            //插入SURVEY_RECORD
            val surveyRecord = new SURVEYRECORD
            surveyRecord.szKNo = k.xckybh
            surveyRecord.szCaseName = k.ajmc
            surveyRecord.szPhyEvidenceNo = k.xcwzbh
            surveyRecord.szJieJingNo = receptionNo
            surveyRecord.nState = survey.SURVEY_STATE_DEFAULT
            surveyRecord.nJieJingState = if (StringUtils.isNotEmpty(receptionNo) && StringUtils.isNotBlank(receptionNo))
              survey.SURVEY_STATE_SUCCESS
            else survey.SURVEY_STATE_DEFAULT
            surveyRecordService.addSurveyRecord(surveyRecord)
          }
        } catch {
          case ex: Exception =>
            error("getLatentList-error:{},currentTime:{}"
              , ExceptionUtil.getStackTraceInfo(ex), DateConverter.convertDate2String(new Date, SurveyConstant.DATETIME_FORMAT)
            )
        } finally {
          surveyConfig.nSeq = 1
          //surveyConfig.szStartTime = jssj
          surveyConfig.szEndTime = ""
          surveyConfigService.updateSurveyConfig(surveyConfig)
        }
      }

      //      var nIndex = 1
      //      val step = 9
      //      Range(nIndex, latentCount,10).foreach{i =>
      //        val ks = i
      //        var js = ks + step
      //        if(js > latentCount)
      //          js = latentCount
      //        val fingerPrintListResponse = fPT50HandprintServiceClient.getLatentList(surveyConfig.szUnitCode, hallWebserviceConfig.handprintService.dataType, "", minusDateTime(kssj), jssj, ks, js)
      //        if(fingerPrintListResponse.nonEmpty){
      //          info("单位代码:{} 现场列表大小:{}",surveyConfig.szUnitCode,fingerPrintListResponse.get.list.length)
      //          try {
      //            fingerPrintListResponse.get.list.foreach { k =>
      //              val xckybh = k.xckybh
      //              //根据现堪编号获取接警编号
      //              val receptionNo = fPT50HandprintServiceClient.getReceptionNo(xckybh)
      //              //插入SURVEY_RECORD
      //              val surveyRecord = new SURVEYRECORD
      //              surveyRecord.szKNo = k.xckybh
      //              surveyRecord.szCaseName = k.ajmc
      //              surveyRecord.szPhyEvidenceNo = k.xcwzbh
      //              surveyRecord.szJieJingNo = receptionNo
      //              surveyRecord.nState = survey.SURVEY_STATE_DEFAULT
      //              surveyRecord.nJieJingState = if(StringUtils.isNotEmpty(receptionNo) && StringUtils.isNotBlank(receptionNo))
      //                survey.SURVEY_STATE_SUCCESS
      //              else survey.SURVEY_STATE_DEFAULT
      //
      //              surveyRecordService.addSurveyRecord(surveyRecord)
      //              nIndex += 1
      //            }
      //          }catch {
      //            case ex: Exception=>
      //              error("getLatentList-error:{},currentTime:{}"
      //                ,ExceptionUtil.getStackTraceInfo(ex),DateConverter.convertDate2String(new Date,SurveyConstant.DATETIME_FORMAT)
      //              )
      //          }finally {
      //            surveyConfig.nSeq = 1
      //            surveyConfigService.updateSurveyConfig(surveyConfig)
      //          }
      //        }
      //      }
      //      if (nIndex != latentCount) {
      //        surveyConfig.szStartTime = kssj
      //        surveyConfig.szEndTime = systemDateTime
      //      } else {
      //        surveyConfig.szStartTime = jssj
      //        surveyConfig.szEndTime = ""
      //      }
      //      surveyConfig.nSeq = nIndex
      //      surveyConfig.szStartTime = jssj
      //      surveyConfig.szEndTime = ""
      //      surveyConfigService.updateSurveyConfig(surveyConfig)
      //    }else if(latentCount == 1){
      //      val fingerPrintListResponse = fPT50HandprintServiceClient.getLatentList(surveyConfig.szUnitCode, hallWebserviceConfig.handprintService.dataType, "", minusDateTime(kssj), jssj, 1, 1)
      //      if(fingerPrintListResponse.nonEmpty){
      //        info("单位代码:{} 现场列表大小:{}",surveyConfig.szUnitCode,fingerPrintListResponse.get.list.length)
      //        try {
      //          fingerPrintListResponse.get.list.foreach { k =>
      //            val xckybh = k.xckybh
      //            //根据现堪编号获取接警编号
      //            val receptionNo = fPT50HandprintServiceClient.getReceptionNo(xckybh)
      //            //插入SURVEY_RECORD
      //            val surveyRecord = new SURVEYRECORD
      //            surveyRecord.szKNo = k.xckybh
      //            surveyRecord.szCaseName = k.ajmc
      //            surveyRecord.szPhyEvidenceNo = k.xcwzbh
      //            surveyRecord.szJieJingNo = receptionNo
      //            surveyRecord.nState = survey.SURVEY_STATE_DEFAULT
      //            surveyRecord.nJieJingState = if(StringUtils.isNotEmpty(receptionNo) && StringUtils.isNotBlank(receptionNo))
      //              survey.SURVEY_STATE_SUCCESS
      //            else survey.SURVEY_STATE_DEFAULT
      //
      //            surveyRecordService.addSurveyRecord(surveyRecord)
      //            surveyConfig.szStartTime = jssj
      //            surveyConfig.szEndTime = ""
      //            surveyConfigService.updateSurveyConfig(surveyConfig)
      //          }
      //        }catch {
      //          case ex: Exception=>
      //            error("getLatentList-error:{},currentTime:{}"
      //              ,ExceptionUtil.getStackTraceInfo(ex),DateConverter.convertDate2String(new Date,SurveyConstant.DATETIME_FORMAT)
      //            )
      //        }
      //      }
      //    }
    }else {
        //没有获取到数据，设置开始时间为结束时间, 开始位置1，结束时间为空,下次任务会以系统时间为结束时间
        surveyConfig.nSeq = 1
        surveyConfig.szStartTime = jssj
        surveyConfig.szEndTime = ""

        surveyConfigService.updateSurveyConfig(surveyConfig)
      }

    }

    /**
      * 调整开始时间提前5天
      *
      * @param time
      * @return
      */
    private def minusDateTime(time: String): String = {
      val myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val date = myFmt.parse(time)
      val rightNow = Calendar.getInstance()
      rightNow.setTime(date)
      rightNow.add(Calendar.DAY_OF_YEAR, -5) // 日期减5天
      myFmt.format(rightNow.getTime)
    }

    def checkJinZhiGetLatentListService: Unit = {
      info("start checkJinZhiGetLatentListService")
      if (hallWebserviceConfig.handprintService.area != null) {
        if (LogInterfacestatus.find_by_asjfsddXzqhdm_and_interfacename(hallWebserviceConfig.handprintService.area.toInt, "getLatentList").nonEmpty) {
          info("update-logInterfaceStatus,行政区划{},接口名称{}", hallWebserviceConfig.handprintService.area, "getLatentList")
          LogInterfacestatus.update.set(calltime = new Date()).where(LogInterfacestatus.asjfsddXzqhdm === hallWebserviceConfig.handprintService.area.toInt and LogInterfacestatus.interfacename === "getLatentList").execute
        } else {
          val logInterfaceStatus = new LogInterfacestatus()
          info("insert-logInterfaceStatus,行政区划{},接口名称{}", hallWebserviceConfig.handprintService.area, "getLatentList")
          logInterfaceStatus.pkId = UUID.randomUUID().toString.replace("-", "")
          logInterfaceStatus.interfacename = "getLatentList"
          logInterfaceStatus.schedule = CronExpParser.translate(hallWebserviceConfig.handprintService.cron)
          logInterfaceStatus.asjfsddXzqhdm = hallWebserviceConfig.handprintService.area.toInt
          logInterfaceStatus.calltime = new Date()
          logInterfaceStatus.save()
        }
      }
      info("end checkJinZhiGetLatentListService")
    }

  /**
    * 程序执行创建config表信息
    */
  def createSurveyConfig():Unit = {
    try {
      info("begin createSurveyConfig")
      //根据配置里的62应用服务器地址，使用动态变量访问
      if (hallWebserviceConfig.handprintService.surveyV62ServiceConfig != null) {
        hallWebserviceConfig.handprintService.surveyV62ServiceConfig.foreach { surveyV62ServiceConfig =>
          V62Facade.withConfigurationServer(surveyV62ServiceConfig.v62ServerConfig) {
            initSurveyConfig(surveyV62ServiceConfig.surveyConfig)
          }
        }
      }
      info("end  createSurveyConfig")
    } catch {
      case ex: Exception =>
        error("createSurveyConfig-error:{},currentTime:{}"
          , ExceptionUtil.getStackTraceInfo(ex), DateConverter.convertDate2String(new Date, SurveyConstant.DATETIME_FORMAT)
        )
    }
  }
}