package nirvana.hall.v70.internal.adapter.common.service

import nirvana.hall.api.services.CaseInfoService
import nirvana.hall.c.services.gfpt4lib.FPT4File.Logic03Rec
import nirvana.hall.protocol.api.FPTProto.Case
import nirvana.hall.v70.common.jpa.SysUser
import nirvana.hall.v70.internal.{CommonUtils, Gafis70Constants}
import nirvana.hall.v70.internal.sync.ProtobufConverter
import nirvana.hall.v70.internal.adapter.common.jpa._
import nirvana.hall.v70.services.sys.UserService
import org.springframework.transaction.annotation.Transactional

/**
  * Created by songpeng on 16/1/26.
  */
class CaseInfoServiceImpl(userService: UserService) extends CaseInfoService{

  /**
    * 新增案件信息
    * @param caseInfo
    * @return
    */
  @Transactional
  override def addCaseInfo(caseInfo: Case, dbId: Option[String]): Unit = {
    val gafisCase = ProtobufConverter.convertCase2GafisCase(caseInfo)
    var user = userService.findSysUserByLoginName(gafisCase.inputpsn)
    if (user.isEmpty){//找不到对应的用户，使用管理员用户
      user = Option(SysUser.find(Gafis70Constants.INPUTPSN))
    }
    gafisCase.inputpsn = user.get.pkId
    gafisCase.createUnitCode = user.get.departCode
    val modUser = userService.findSysUserByLoginName(gafisCase.modifiedpsn)
    if(modUser.nonEmpty){
      gafisCase.modifiedpsn = modUser.get.pkId
    }

    gafisCase.deletag = Gafis70Constants.DELETAG_USE
    gafisCase.caseSource = caseInfo.getStrDataSource
    gafisCase.save()
    val logicDb:GafisLogicDb = if(dbId == None || dbId.get.length <= 0){
      GafisLogicDb.where(GafisLogicDb.logicCategory === "1").and(GafisLogicDb.logicIsdefaulttag === "1").headOption.get
    }else{
      GafisLogicDb.find(dbId.get)
    }
    //逻辑库
    val logicDbCase = new GafisLogicDbCase()
    logicDbCase.pkId = CommonUtils.getUUID()
    logicDbCase.logicDbPkid = logicDb.pkId
    logicDbCase.casePkid = gafisCase.caseId
    logicDbCase.save()
  }

  /**
    * 更新案件信息
    * @param caseInfo
    * @return
    */
  @Transactional
  override def updateCaseInfo(caseInfo: Case, dbId: Option[String]): Unit = {
    val gafisCase = GafisCase.find(caseInfo.getStrCaseID)
    ProtobufConverter.convertCase2GafisCase(caseInfo, gafisCase)

    var user = userService.findSysUserByLoginName(gafisCase.inputpsn)
    if (user.isEmpty){//找不到对应的用户，使用管理员用户
      user = Option(SysUser.find(Gafis70Constants.INPUTPSN))
    }
    gafisCase.inputpsn = user.get.pkId
    gafisCase.createUnitCode = user.get.departCode
    val modUser = userService.findSysUserByLoginName(gafisCase.modifiedpsn)
    if(modUser.nonEmpty){
      gafisCase.modifiedpsn = modUser.get.pkId
    }

    gafisCase.deletag = Gafis70Constants.DELETAG_USE
    gafisCase.caseSource = caseInfo.getStrDataSource
    gafisCase.save()

    //删除原来的逻辑库
    GafisLogicDbCase.delete.where(GafisLogicDbCase.casePkid === gafisCase.caseId)
    //保存逻辑库
    val logicDb:GafisLogicDb = if(dbId == None || dbId.get.length <= 0){
      GafisLogicDb.where(GafisLogicDb.logicCategory === "1").and(GafisLogicDb.logicIsdefaulttag === "1").headOption.get
    }else{
      GafisLogicDb.find(dbId.get)
    }
    //逻辑库
    val logicDbCase = new GafisLogicDbCase()
    logicDbCase.pkId = CommonUtils.getUUID()
    logicDbCase.logicDbPkid = logicDb.pkId
    logicDbCase.casePkid = gafisCase.caseId
    logicDbCase.save()

  }

  /**
    * 获取案件信息
    * @param caseId
    * @return
    */
  override def getCaseInfo(caseId: String, dbId: Option[String]): Case= {
    val gafisCase = GafisCase.findOption(caseId)
    val fingers = GafisCaseFinger.select(GafisCaseFinger.fingerId).where(GafisCaseFinger.caseId === caseId).toList.asInstanceOf[List[String]]
    if(gafisCase.isEmpty){
      throw new RuntimeException("记录不存在!");
    }
    ProtobufConverter.convertGafisCase2Case(gafisCase.get,fingers)
  }

  /**
    * 删除案件信息
    * @param caseId
    * @return
    */
  override def delCaseInfo(caseId: String, dbId: Option[String]): Unit = {
    GafisCase.update.set(deletag = Gafis70Constants.DELETAG_DEL).where(GafisCase.caseId === caseId).execute
  }

  /**
    * 验证案件编号是否已存在
    * @param caseId
    * @return
    */
  override def isExist(caseId: String, dbId: Option[String]): Boolean = {
    GafisCase.findOption(caseId).nonEmpty
  }

  /**
    * 查询案件编号列表
    *
    * @param ajno        案件编号
    * @param ajlb        案件类别
    * @param fadddm      发案地代码
    * @param mabs        命案标识
    * @param xcjb        协查级别
    * @param xcdwdm      查询单位代码
    * @param startfadate 开始时间（检索发案时间，时间格式YYYYMMDD）
    * @param endfadate   结束时间（检索发案时间，时间格式YYYYMMDD）
    * @return
    */
  override def getFPT4Logic03RecList(ajno: String, ajlb: String, fadddm: String, mabs: String, xcjb: String, xcdwdm: String, startfadate: String, endfadate: String): Seq[Logic03Rec] = {
    throw new UnsupportedOperationException
  }

}
