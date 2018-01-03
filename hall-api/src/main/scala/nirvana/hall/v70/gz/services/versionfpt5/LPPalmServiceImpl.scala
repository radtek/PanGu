package nirvana.hall.v70.gz.services.versionfpt5

import java.util.{Date, UUID}
import javax.persistence.EntityManager

import com.google.protobuf.ByteString
import monad.support.services.LoggerSupport
import nirvana.hall.api.internal.DateConverter
import nirvana.hall.api.services.LPPalmService
import nirvana.hall.protocol.api.FPTProto._
import nirvana.hall.v70.gz.Constant
import nirvana.hall.v70.gz.jpa.{GafisCasePalm, GafisCasePalmMnt, SysUser}
import nirvana.hall.v70.gz.sync._
import nirvana.hall.v70.gz.sys.UserService
import nirvana.hall.v70.internal.Gafis70Constants
import org.springframework.transaction.annotation.Transactional

/**
  * 现场掌纹service实现
  */
class LPPalmServiceImpl(entityManager: EntityManager, userService: UserService) extends LPPalmService with LoggerSupport{

  /**
    * 新增现场卡片
    *
    * @param lpCard
    * @return
    */
  @Transactional
  override def addLPCard(lpCard: LPCard, dbId: Option[String]): Unit = {
    val casePalm= convertLPCard2GafisCasePalm(lpCard)
    val casePalmMnt = ProtobufConverter.convertLPCard2GafisCasePalmMnt(lpCard)
    val nativeQuery = entityManager.createNativeQuery("select gafis_case_sid_seq.nextval from dual")
    val sid = java.lang.Long.parseLong(nativeQuery.getResultList.get(0).toString)
    casePalm.sid = sid
    //将用户名转为用户id
    var user = userService.findSysUserByLoginName(casePalm.inputpsn)
    if (user.isEmpty){//找不到对应的用户，使用管理员用户
      user = Option(SysUser.find(Gafis70Constants.INPUTPSN))
    }
    casePalm.inputpsn = user.get.pkId
    casePalm.inputtime = new Date
    casePalm.creatorUnitCode = user.get.departCode
    val modUser = userService.findSysUserByLoginName(casePalm.modifiedpsn)
    if(modUser.nonEmpty){
      casePalm.modifiedpsn = modUser.get.pkId
    }else{
      casePalm.modifiedpsn = ""
    }
    casePalm.deletag = Gafis70Constants.DELETAG_USE
    casePalm.save()

    casePalmMnt.pkId = UUID.randomUUID().toString.replace("-",Constant.EMPTY)
    casePalmMnt.inputpsn = user.get.pkId
    casePalmMnt.isMainMnt = Gafis70Constants.IS_MAIN_MNT
    casePalmMnt.deletag = Gafis70Constants.DELETAG_USE
    casePalmMnt.save()
    info("addLPPalm cardId:{}", lpCard.getStrCardID)
  }

  /**
    * 获取现场卡片
    *
    * @param palmId
    * @return
    */
  override def getLPCard(palmId: String, dbId: Option[String]): LPCard = {
    val casePalm = GafisCasePalm.find(palmId)
    val casePalmMnt = GafisCasePalmMnt.where(GafisCasePalmMnt.palmId === palmId).and(GafisCasePalmMnt.isMainMnt === "1").headOption.get
    convertGafisCasePalm2LPCard(casePalm, casePalmMnt)
  }

  /**
    * 更新现场卡片
    *
    * @param lpCard
    * @return
    */
  @Transactional
  override def updateLPCard(lpCard: LPCard, dbId: Option[String]): Unit = {
    val casePalm = GafisCasePalm.find(lpCard.getStrCardID)
    convertLPCard2GafisCasePalm(lpCard, casePalm)
    //将用户名转为用户id
    var user = userService.findSysUserByLoginName(casePalm.inputpsn)
    if (user.isEmpty){//找不到对应的用户，使用管理员用户
      user = Option(SysUser.find(Gafis70Constants.INPUTPSN))
    }
    casePalm.inputpsn = user.get.pkId
    casePalm.creatorUnitCode= user.get.departCode
    val modUser = userService.findSysUserByLoginName(casePalm.modifiedpsn)
    if(modUser.nonEmpty){
      casePalm.modifiedpsn = modUser.get.pkId
    }else{
      casePalm.modifiedpsn = ""
    }
    casePalm.deletag = Gafis70Constants.DELETAG_USE
    casePalm.save()

    val casePalmMnt = ProtobufConverter.convertLPCard2GafisCasePalmMnt(lpCard)
    //先删除，后插入
    GafisCasePalmMnt.delete.where(GafisCasePalmMnt.palmId === casePalm.palmId).execute
    casePalmMnt.pkId =  UUID.randomUUID().toString.replace("-",Constant.EMPTY)
    casePalmMnt.inputpsn = user.get.pkId
    casePalmMnt.deletag = Gafis70Constants.DELETAG_USE
    casePalmMnt.save()

    info("addLPPalm cardId:{}", lpCard.getStrCardID)
  }

  /**
    * 删除现场卡片
    *
    * @param cardId
    * @return
    */
  @Transactional
  override def delLPCard(cardId: String, dbId: Option[String]): Unit = ???

  /**
    * 验证现场卡片是否存在
    *
    * @param cardId
    * @return
    */
  override def isExist(cardId: String, dbId: Option[String]): Boolean = {
    GafisCasePalm.findOption(cardId).nonEmpty
  }

  /**
    * 现场指纹转为protobuf
    *
    * @param casePalm
    * @param casePalmMnt
    * @return
    */
  def convertGafisCasePalm2LPCard(casePalm: GafisCasePalm, casePalmMnt: GafisCasePalmMnt): LPCard = {
    val lpCard = LPCard.newBuilder()
    lpCard.setStrCardID(casePalm.palmId)
    //5.0新加
    lpCard.setStrPhysicalId(casePalm.physicalEvidenceNo)

    val textBuilder = lpCard.getTextBuilder
    magicSet(casePalm.seqNo, textBuilder.setStrSeq)
    if ("1".equals(casePalm.isCorpse))
      textBuilder.setBDeadBody(true)
    magicSet(casePalm.corpseNo, textBuilder.setStrDeadPersonNo)
    magicSet(casePalm.remainPlace, textBuilder.setStrRemainPlace)
    magicSet(casePalm.ridgeColor, textBuilder.setStrRidgeColor)
    textBuilder.setNXieChaState(casePalm.isAssist)
    textBuilder.setNBiDuiState(casePalm.matchStatus)

    val blobBuilder = lpCard.getBlobBuilder
    blobBuilder.setType(ImageType.IMAGETYPE_PALM)
    blobBuilder.setStImageBytes(ByteString.copyFrom(casePalm.palmImg))
    //特征
    if(casePalmMnt.palmMnt != null)
      blobBuilder.setStMntBytes(ByteString.copyFrom(casePalmMnt.palmMnt))
    magicSet(casePalmMnt.captureMethod, blobBuilder.setStrMntExtractMethod)
    //指位
    casePalm.fgp match {
      case "11" =>
        blobBuilder.setPalmFgp(PalmFgp.PALM_RIGHT)
      case "12" =>
        blobBuilder.setPalmFgp(PalmFgp.PALM_LEFT)
      case "13" =>
        blobBuilder.setPalmFgp(PalmFgp.PALM_FOUR_PRINT_RIGHT)
      case "14" =>
        blobBuilder.setPalmFgp(PalmFgp.PALM_FOUR_PRINT_LEFT)
      case "15" =>
        blobBuilder.setPalmFgp(PalmFgp.PALM_FULL_PALM_RIGHT)
      case "16" =>
        blobBuilder.setPalmFgp(PalmFgp.PALM_FULL_PALM_LEFT)
      case _ =>
        blobBuilder.setPalmFgp(PalmFgp.PALM_UNKNOWN)
    }
//
//    if (isNonBlank(casePalm.fgp))
//      0.until(casePalm.fgp.length)
//        .filter("1" == casePalm.fgp.charAt(_))
//        .foreach(i => blobBuilder.addFgp(FingerFgp.valueOf(i + 1)))
    //纹型
    if (isNonBlank(casePalm.pattern))
      casePalm.pattern.split(",").foreach(f => blobBuilder.addRp(PatternType.valueOf(f)))

    lpCard.build()
  }

  /**
    * 现场掌纹protobuf转为GafisPalm
    *
    * @param lpCard
    * @param casePalm
    * @return
    */
  def convertLPCard2GafisCasePalm(lpCard: LPCard, casePalm: GafisCasePalm = new GafisCasePalm()): GafisCasePalm = {
    casePalm.palmId = lpCard.getStrCardID
    //5.0新加
    casePalm.physicalEvidenceNo = lpCard.getStrPhysicalId
    val text = lpCard.getText
    casePalm.caseId = text.getStrCaseId
    casePalm.seqNo = text.getStrSeq
    casePalm.remainPlace = text.getStrRemainPlace
    casePalm.ridgeColor = text.getStrRidgeColor
    casePalm.isCorpse = if(text.getBDeadBody) "1" else "0"
    casePalm.corpseNo = text.getStrDeadPersonNo
    casePalm.isAssist = text.getNXieChaState.toString
    casePalm.matchStatus = text.getNBiDuiState.toString
    casePalm.developMethod = text.getStrCaptureMethod
    casePalm.remark = text.getStrComment

    //操作信息
    val admData = lpCard.getAdmData
    if(admData != null){
      casePalm.inputpsn = admData.getCreator
      casePalm.modifiedpsn = admData.getUpdator
      casePalm.creatorUnitCode = admData.getCreateUnitCode
      if(admData.getCreateDatetime != null && admData.getCreateDatetime.length == 14){
        casePalm.inputtime = DateConverter.convertString2Date(admData.getCreateDatetime, "yyyyMMddHHmmss")
      }
      if(admData.getUpdateDatetime != null && admData.getUpdateDatetime.length == 14){
        casePalm.modifiedtime = DateConverter.convertString2Date(admData.getUpdateDatetime, "yyyyMMddHHmmss")
      }
    }

    val blob = lpCard.getBlob
    casePalm.palmImg = blob.getStImageBytes.toByteArray
    casePalm
  }
}