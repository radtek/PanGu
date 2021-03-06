package nirvana.hall.v70.internal.sync

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.Date

import com.google.protobuf.ByteString
import monad.support.services.LoggerSupport
import nirvana.hall.api.internal.DateConverter
import nirvana.hall.c.services.gloclib.glocdef
import nirvana.hall.c.services.gloclib.glocdef.GAFISMICSTRUCT
import nirvana.hall.protocol.api.FPTProto.{MatchRelationInfo, _}
import nirvana.hall.protocol.matcher.MatchResultProto.MatchResult
import nirvana.hall.protocol.matcher.MatchTaskQueryProto.MatchTask
import nirvana.hall.protocol.matcher.MatchTaskQueryProto.MatchTask.LatentMatchData
import nirvana.hall.protocol.matcher.NirvanaTypeDefinition.MatchType
import nirvana.hall.v62.internal.c.gbaselib.gitempkg
import nirvana.hall.v62.internal.c.gloclib.{galocpkg, galoctp}
import nirvana.hall.v62.services.DictCode6Map7
import nirvana.hall.v70.internal.adapter.common.service.TransSpellName
import nirvana.hall.v70.internal.query.QueryConstants
import nirvana.hall.v70.internal.{CommonUtils, Gafis70Constants}
import nirvana.hall.v70.internal.adapter.common.jpa.{GafisCheckinInfo, _}
import org.jboss.netty.buffer.ChannelBuffers

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * Proto与po实体对象相互转换
  */
object ProtobufConverter extends LoggerSupport{

  /**
    * 案件信息转换
    *
    * @param caseInfo
    * @return
    */
  def convertCase2GafisCase(caseInfo: Case, gafisCase: GafisCase = new GafisCase()): GafisCase = {
    gafisCase.caseId = caseInfo.getStrCaseID
    gafisCase.cardId = caseInfo.getStrCaseID
    val text = caseInfo.getText
    gafisCase.caseClassCode = text.getStrCaseType1
    gafisCase.suspiciousAreaCode = text.getStrSuspArea1Code
    gafisCase.caseOccurDate = text.getStrCaseOccurDate
    gafisCase.caseOccurPlaceCode = text.getStrCaseOccurPlaceCode
    gafisCase.caseOccurPlaceDetail = text.getStrCaseOccurPlace
    gafisCase.assistLevel = DictCode6Map7.assistLevel.get(text.getNSuperviseLevel)
    gafisCase.extractUnitCode = text.getStrExtractUnitCode
    gafisCase.extractUnitName = text.getStrExtractUnitName
    gafisCase.extractor = text.getStrExtractor
    gafisCase.extractDate = text.getStrExtractDate
    gafisCase.amount = text.getStrMoneyLost
    gafisCase.isMurder = if(text.getBPersonKilled) "1" else "0"
    gafisCase.remark = text.getStrComment
    gafisCase.caseState = text.getNCaseState.toString
    gafisCase.assistBonus = text.getStrPremium
    gafisCase.assistSign = text.getNXieChaState.toString
    //    gafisCase.assistRevokeSign = text.getNCancelFlag.toString// TODO值太大
    gafisCase.assistDeptCode = text.getStrXieChaRequestUnitCode
    gafisCase.assistDeptName = text.getStrXieChaRequestUnitName

    //操作信息
    val admData = caseInfo.getAdmData
    if(admData != null){
      gafisCase.inputpsn = admData.getCreator
      gafisCase.modifiedpsn = admData.getUpdator
      gafisCase.createUnitCode = admData.getCreateUnitCode
      if(admData.getCreateDatetime != null && admData.getCreateDatetime.length == 14){
        gafisCase.inputtime = DateConverter.convertString2Date(admData.getCreateDatetime, "yyyyMMddHHmmss")
      }
      if(admData.getUpdateDatetime != null && admData.getUpdateDatetime.length == 14){
        gafisCase.modifiedtime = DateConverter.convertString2Date(admData.getUpdateDatetime, "yyyyMMddHHmmss")
      }
    }

    gafisCase
  }
  def convertGafisCase2Case(caseInfo: GafisCase, fingerIds: Seq[String]): Case = {
    val caseBuilder = Case.newBuilder()
    caseBuilder.setStrCaseID(caseInfo.caseId)
    caseBuilder.setStrDataSource(caseInfo.caseSource)

    val textBuilder = caseBuilder.getTextBuilder
    magicSet(caseInfo.caseClassCode, textBuilder.setStrCaseType1)
    magicSet(caseInfo.caseOccurDate, textBuilder.setStrCaseOccurDate)
    magicSet(caseInfo.caseOccurPlaceCode, textBuilder.setStrCaseOccurPlaceCode)
    magicSet(caseInfo.caseOccurPlaceDetail, textBuilder.setStrCaseOccurPlace)

    magicSet(caseInfo.remark, textBuilder.setStrComment)
    if("1".equals(caseInfo.isMurder))
      textBuilder.setBPersonKilled(true)
    magicSet(caseInfo.amount, textBuilder.setStrMoneyLost)
    magicSet(caseInfo.extractUnitCode, textBuilder.setStrExtractUnitCode)
    magicSet(caseInfo.extractUnitName, textBuilder.setStrExtractUnitName)
    magicSet(caseInfo.extractDate, textBuilder.setStrExtractDate)
    magicSet(caseInfo.extractor, textBuilder.setStrExtractor)
    magicSet(caseInfo.suspiciousAreaCode, textBuilder.setStrSuspArea1Code)
    textBuilder.setNSuperviseLevel(caseInfo.assistLevel)
    magicSet(caseInfo.assistDeptCode, textBuilder.setStrXieChaRequestUnitCode)
    magicSet(caseInfo.assistDeptName, textBuilder.setStrXieChaRequestUnitName)
    magicSet(caseInfo.assistDate, textBuilder.setStrXieChaDate)
    magicSet(caseInfo.assistBonus, textBuilder.setStrPremium)
    textBuilder.setNCaseState(caseInfo.assistSign)
    textBuilder.setNCancelFlag(caseInfo.assistRevokeSign)
    textBuilder.setNCaseState(caseInfo.caseState)

    if(fingerIds != null)
      fingerIds.foreach(f => caseBuilder.addStrFingerID(f))
    caseBuilder.setNCaseFingerCount(caseBuilder.getStrFingerIDCount)

    caseBuilder.build()
  }

  def convertLPCard2GafisCaseFinger(lpCard: LPCard, caseFinger: GafisCaseFinger = new GafisCaseFinger()): GafisCaseFinger = {
    caseFinger.fingerId = lpCard.getStrCardID
    val text = lpCard.getText
    //caseFinger.ltOperator = "8a8187175138054e0151381149490003"
    caseFinger.seqNo = text.getStrSeq
    caseFinger.remainPlace = text.getStrRemainPlace
    caseFinger.ridgeColor = text.getStrRidgeColor
    caseFinger.isCorpse = if(text.getBDeadBody) "1" else "0"
    caseFinger.corpseNo = text.getStrDeadPersonNo
    caseFinger.isAssist = text.getNXieChaState.toString
    caseFinger.matchStatus = text.getNBiDuiState.toString
    caseFinger.mittensBegNo = text.getStrStart
    caseFinger.mittensEndNo = text.getStrEnd
    caseFinger.caseId = text.getStrCaseId
    caseFinger.developMethod = text.getStrCaptureMethod
    caseFinger.remark = text.getStrComment

    //操作信息
    val admData = lpCard.getAdmData
    if(admData != null){
      caseFinger.inputpsn = admData.getCreator
      caseFinger.modifiedpsn = admData.getUpdator
      caseFinger.creatorUnitCode = admData.getCreateUnitCode
      if(admData.getCreateDatetime != null && admData.getCreateDatetime.length == 14){
        caseFinger.inputtime = DateConverter.convertString2Date(admData.getCreateDatetime, "yyyyMMddHHmmss")
      }
      if(admData.getUpdateDatetime != null && admData.getUpdateDatetime.length == 14){
        caseFinger.modifiedtime = DateConverter.convertString2Date(admData.getUpdateDatetime, "yyyyMMddHHmmss")
      }
    }

    val blob = lpCard.getBlob
    caseFinger.fingerImg = blob.getStImageBytes.toByteArray
    caseFinger
  }

  /**
    * 现在指纹protobuf转为现场指纹特征
    *
    * @param lpCard
    * @return
    */
  def convertLPCard2GafisCaseFingerMnt(lpCard: LPCard): GafisCaseFingerMnt = {
    val caseFingerMnt = new GafisCaseFingerMnt()
    caseFingerMnt.fingerId = lpCard.getStrCardID
    val blob = lpCard.getBlob
    caseFingerMnt.fingerMnt = blob.getStMntBytes.toByteArray
    caseFingerMnt.fingerRidge = blob.getStBinBytes.toByteArray
    caseFingerMnt.captureMethod = blob.getStrMntExtractMethod
    caseFingerMnt.isMainMnt = Gafis70Constants.IS_MAIN_MNT
    caseFingerMnt.inputtime = new Date()

    caseFingerMnt
  }

  /**
    * 现场指纹转为protobuf
    *
    * @param caseFinger
    * @param caseFingerMnt
    * @return
    */
  def convertGafisCaseFinger2LPCard(caseFinger: GafisCaseFinger, caseFingerMnt: GafisCaseFingerMnt): LPCard = {
    val lpCard = LPCard.newBuilder()
    lpCard.setStrCardID(caseFinger.fingerId)
    val textBuilder = lpCard.getTextBuilder
    magicSet(caseFinger.caseId, textBuilder.setStrCaseId)
    magicSet(caseFinger.seqNo, textBuilder.setStrSeq)
    if ("1".equals(caseFinger.isCorpse))
      textBuilder.setBDeadBody(true)
    magicSet(caseFinger.corpseNo, textBuilder.setStrDeadPersonNo)
    magicSet(caseFinger.remainPlace, textBuilder.setStrRemainPlace)
    magicSet(caseFinger.ridgeColor, textBuilder.setStrRidgeColor)
    magicSet(caseFinger.mittensBegNo, textBuilder.setStrStart)
    magicSet(caseFinger.mittensEndNo, textBuilder.setStrEnd)
    textBuilder.setNXieChaState(caseFinger.isAssist)
    textBuilder.setNBiDuiState(caseFinger.matchStatus)

    val blobBuilder = lpCard.getBlobBuilder
    blobBuilder.setType(ImageType.IMAGETYPE_FINGER)
    //    magicSet(caseFinger.developMethod, blobBuilder.setStrMntExtractMethod)
    blobBuilder.setStImageBytes(ByteString.copyFrom(caseFinger.fingerImg))
    //特征
    if(caseFingerMnt.fingerMnt != null)
      blobBuilder.setStMntBytes(ByteString.copyFrom(caseFingerMnt.fingerMnt))
    magicSet(caseFingerMnt.captureMethod, blobBuilder.setStrMntExtractMethod)
    //指位
    if (isNonBlank(caseFinger.fgp))
      0.until(caseFinger.fgp.length)
        .filter("1" == caseFinger.fgp.charAt(_))
        .foreach(i => blobBuilder.addFgp(FingerFgp.valueOf(i + 1)))
    //纹型
    if (isNonBlank(caseFinger.pattern))
      caseFinger.pattern.split(",").foreach(f => blobBuilder.addRp(PatternType.valueOf(f)))

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

  /**
    * 现场掌纹特征转换
    *
    * @param lpCard
    * @return
    */
  def convertLPCard2GafisCasePalmMnt(lpCard: LPCard, casePalmMnt: GafisCasePalmMnt = new GafisCasePalmMnt()): GafisCasePalmMnt = {
    casePalmMnt.palmId = lpCard.getStrCardID
    val blob = lpCard.getBlob
    casePalmMnt.palmMnt = blob.getStMntBytes.toByteArray
    casePalmMnt.palmRidge = blob.getStBinBytes.toByteArray
    casePalmMnt.captureMethod = blob.getStrMntExtractMethod
    casePalmMnt.isMainMnt = Gafis70Constants.IS_MAIN_MNT
    casePalmMnt.inputtime = new Date()

    casePalmMnt
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
    blobBuilder.setType(ImageType.IMAGETYPE_FINGER)
    blobBuilder.setStImageBytes(ByteString.copyFrom(casePalm.palmImg))
    //特征
    if(casePalmMnt.palmMnt != null)
      blobBuilder.setStMntBytes(ByteString.copyFrom(casePalmMnt.palmMnt))
    magicSet(casePalmMnt.captureMethod, blobBuilder.setStrMntExtractMethod)
    //指位
    if (isNonBlank(casePalm.fgp))
      0.until(casePalm.fgp.length)
        .filter("1" == casePalm.fgp.charAt(_))
        .foreach(i => blobBuilder.addFgp(FingerFgp.valueOf(i + 1)))
    //纹型
    if (isNonBlank(casePalm.pattern))
      casePalm.pattern.split(",").foreach(f => blobBuilder.addRp(PatternType.valueOf(f)))

    lpCard.build()
  }

  /**
    * 捺印人员信息转为protobuf
    *
    * @param person
    * @param photoList
    * @param fingerList
    * @param palmList
    * @return
    */
  def convertGafisPerson2TPCard(person: GafisPerson,photoList: Seq[GafisGatherPortrait], fingerList: Seq[GafisGatherFinger], palmList: Seq[GafisGatherPalm]): TPCard={
    val tpCard = TPCard.newBuilder()
    tpCard.setStrCardID(person.personid)
    tpCard.setStrPersonID(person.personid)
    tpCard.setStrDataSource(person.dataSources.toString)

    //文本信息
    val textBuilder = tpCard.getTextBuilder
    magicSet(person.name, textBuilder.setStrName)
    magicSet(person.aliasname, textBuilder.setStrAliasName)
    if (isNonBlank(person.sexCode))
      textBuilder.setNSex(Integer.parseInt(person.sexCode))
    magicSet(person.idcardno, textBuilder.setStrIdentityNum)
    textBuilder.setStrBirthDate(person.birthdayst)
    magicSet(person.door, textBuilder.setStrHuKouPlaceCode)
    magicSet(person.doordetail, textBuilder.setStrHuKouPlaceTail)
    magicSet(person.nationCode, textBuilder.setStrRace)
    magicSet(person.nativeplaceCode, textBuilder.setStrNation)
    magicSet(person.caseClasses, textBuilder.setStrCaseType1)
    magicSet(person.caseClasses2, textBuilder.setStrCaseType2)
    magicSet(person.caseClasses3, textBuilder.setStrCaseType3)
    magicSet(person.address, textBuilder.setStrAddrCode)
    magicSet(person.addressdetail, textBuilder.setStrAddr)
    magicSet(person.personType, textBuilder.setStrPersonType)

    magicSet(person.gatherdepartcode, textBuilder.setStrPrintUnitCode)
    magicSet(person.gatherdepartname, textBuilder.setStrPrintUnitName)
    magicSet(person.gatherusername, textBuilder.setStrPrinter)
    textBuilder.setStrPrintDate(person.gatherDate)
    magicSet(person.remark, textBuilder.setStrComment)

    //协查信息
    textBuilder.setNXieChaFlag(person.assistSign)
    textBuilder.setNXieChaLevel(person.assistLevel)
    magicSet(person.assistBonus, textBuilder.setStrPremium)
    magicSet(person.assistDate, textBuilder.setStrXieChaDate)
    magicSet(person.assistDeptCode, textBuilder.setStrXieChaRequestUnitCode)
    magicSet(person.assistDeptName, textBuilder.setStrXieChaRequestUnitName)
    magicSet(person.assistPurpose, textBuilder.setStrXieChaForWhat)
    magicSet(person.assistRefPerson, textBuilder.setStrRelPersonNo)
    magicSet(person.assistRefCase, textBuilder.setStrRelCaseNo)
    magicSet(person.assistValidDate, textBuilder.setStrXieChaTimeLimit)
    magicSet(person.assistContacts, textBuilder.setStrXieChaContacter)
    magicSet(person.assistNumber, textBuilder.setStrXieChaTelNo)
    magicSet(person.assistApproval, textBuilder.setStrShenPiBy)

    //指纹数据
    val mntMap = fingerList.filter(_.groupId == 0).map(f => ((f.fgpCase, f.fgp), f.gatherData)).toMap[(String, Short), Array[Byte]]
    //纹线特征数据
    val binMap = fingerList.filter(_.groupId == 4).map(f => ((f.fgpCase, f.fgp), f.gatherData)).toMap[(String, Short), Array[Byte]]
    fingerList.filter(_.groupId == 1).foreach {finger =>
      val blobBuilder = tpCard.addBlobBuilder()
      val mnt = mntMap.get((finger.fgpCase, finger.fgp))
      val bin = binMap.get((finger.fgpCase, finger.fgp))
      mnt.foreach { blob =>
        blobBuilder.setStMntBytes(ByteString.copyFrom(blob))
      }
      bin.foreach{
        blob => blobBuilder.setStBinBytes(ByteString.copyFrom(blob))
      }
      blobBuilder.setStImageBytes(ByteString.copyFrom(finger.gatherData))
      blobBuilder.setType(ImageType.IMAGETYPE_FINGER)
      blobBuilder.setBPlain("1".equals(finger.fgpCase))
      blobBuilder.setFgp(FingerFgp.valueOf(finger.fgp))
    }

    //人像数据
    photoList.foreach{ photo =>
      val blobBuilder = tpCard.addBlobBuilder()
      blobBuilder.setStImageBytes(ByteString.copyFrom(photo.gatherData))
      blobBuilder.setType(ImageType.IMAGETYPE_FACE)
      photo.fgp match {
        case Gafis70Constants.FACE_FRONT => blobBuilder.setFacefgp(FaceFgp.FACE_FRONT)
        case Gafis70Constants.FACE_RIGHT => blobBuilder.setFacefgp(FaceFgp.FACE_RIGHT)
        case Gafis70Constants.FACE_LEFT => blobBuilder.setFacefgp(FaceFgp.FACE_LEFT)
      }
    }

    //掌纹数据
    if (palmList != null){
      palmList.foreach{ palm =>
        val blobBuilder = tpCard.addBlobBuilder()
        blobBuilder.setStImageBytes(ByteString.copyFrom(palm.gatherData))
        blobBuilder.setType(ImageType.IMAGETYPE_PALM)
        palm.fgp match {
          case 11 => blobBuilder.setPalmfgp(PalmFgp.PALM_RIGHT)
          case 12 => blobBuilder.setPalmfgp(PalmFgp.PALM_LEFT)
          case other => blobBuilder.setPalmfgp(PalmFgp.PALM_UNKNOWN)
        }
      }
    }

    tpCard.build()
  }

  /**
    * 将tpcard
    *
    * @param tpCard
    * @param person
    * @return
    */
  def convertTPCard2GafisPerson(tpCard: TPCard, person: GafisPerson = new GafisPerson()): GafisPerson={
    person.personid = tpCard.getStrCardID
    person.cardid = tpCard.getStrCardID
    if(person.fingerrepeatno == null){
      person.fingerrepeatno = tpCard.getStrMisPersonID//人员信息编号(重卡)
    }
    val text = tpCard.getText
    person.remark = text.getStrComment//备注
    person.idcardno = text.getStrIdentityNum//身份证号码
    person.address = getCode7to6(DictCode6Map7.areaClasses_nj.toMap,text.getStrAddrCode)//现住址代码(转码)
    person.addressdetail = text.getStrAddr//现住址
    person.doordetail = text.getStrHuKouPlaceTail//户口所在地--户籍地详细住址
    person.gatherdepartname = text.getStrPrintUnitName//捺印人单位--采集单位名称
    person.nationCode = getCode7to6(DictCode6Map7.nation_nj.toMap,text.getStrRace)//民族(字典)
    person.micUpdateUsername = text.getStrMicUpdatorUsername//特征更改用户
    person.micUpdateUnitcode = text.getStrMicUpdatorUnitcode//特征更改单位
    person.psisNo = text.getStrPsisNo //警务平台编号
    person.sexCode = text.getNSex.toString//性别(字典)
    person.personCategory = text.getStrPersonType//人员类型
    person.name = text.getStrName//姓名
    person.spellname = text.getStrSpellName//姓名拼音
    person.aliasname = text.getStrAliasName//别名绰号
    if(text.getStrBirthDate != null){
      person.birthdayst = DateConverter.convertString2Date(text.getStrBirthDate, "yyyyMMdd") //出生日期起
    }
    person.caseClasses = DictCodeCaseClass6to7Reg.caseClassDict6to7(text.getStrCaseType1).getOrElse(getCode7to6(DictCode6Map7.caseClasses_nj.toMap,text.getStrCaseType1))//案件类别
    person.caseClasses2 = DictCodeCaseClass6to7Reg.caseClassDict6to7(text.getStrCaseType2).getOrElse(getCode7to6(DictCode6Map7.caseClasses_nj.toMap,text.getStrCaseType2))
    person.caseClasses3 = DictCodeCaseClass6to7Reg.caseClassDict6to7(text.getStrCaseType3).getOrElse(getCode7to6(DictCode6Map7.caseClasses_nj.toMap,text.getStrCaseType3))
    person.door = getCode7to6(DictCode6Map7.areaClasses_nj.toMap,text.getStrHuKouPlaceCode)//户籍地代码(区域)
    if(text.getStrPersonClassCode != null){
      person.personType = getCode7to6(DictCode6Map7.personType_nj.toMap,text.getStrPersonClassCode)//人员类别代码
    }
    if(text.getStrPrintDate != null){
      person.gatherDate = DateConverter.convertString2Date(text.getStrPrintDate, "yyyyMMddHHmmss") //采集时间
    }
    person.gatherusername = text.getStrPrinter//采集人name
    person.gatherdepartcode = text.getStrPrintUnitCode//采集单位
    person.nativeplaceCode = text.getStrNation//籍贯国籍(字典)
    person.certificatetype = getCode7to6(DictCode6Map7.certificatetype_nj.toMap,text.getStrCertifType)//证件类型code_zjzl
    person.certificateid = text.getStrCertifID//证件号码
    person.recordmark = if(text.getBHasCriminalRecord) '1'.toString else '0'.toString //前科库标识 1：有；0：无
    person.recordsituation = text.getStrCriminalRecordDesc//前科劣迹情况

    //    if(person.idcardno.length > 18){
    //      person.idcardno = person.idcardno.substring(0, 18)
    //    }

    //操作信息
    val admData = tpCard.getAdmData
    if(admData != null){
      if(admData.getCreateDatetime != null && admData.getCreateDatetime.length == 14){
        person.inputtime = DateConverter.convertString2Date(admData.getCreateDatetime, "yyyyMMddHHmmss")
      }else{
        person.inputtime = new Date
      }
      if(admData.getUpdateDatetime != null && admData.getUpdateDatetime.length == 14){
        person.modifiedtime = DateConverter.convertString2Date(admData.getUpdateDatetime, "yyyyMMddHHmmss")
      }

      person.gatherOrgCode = admData.getCreateUnitCode
      person.modifyUnitCode = admData.getUpdateUnitCode
      person.inputUsername = admData.getCreator
      person.modifyUsername = admData.getUpdator
      if(person.caseFingerGroupNo == null){
        person.caseFingerGroupNo = admData.getStrGroupName
      }
    }
    person
  }

  /**
    * 指纹转换
    *
    * @param tpCard
    * @return
    */
  def convertTPCard2GafisGatherFinger(tpCard: TPCard): Seq[GafisGatherFinger] = {
    val fingerList = new ArrayBuffer[GafisGatherFinger]()
    val personId = tpCard.getStrCardID
    val iter = tpCard.getBlobList.iterator()
    while (iter.hasNext){
      val blob = iter.next()
      if(blob.getType == ImageType.IMAGETYPE_FINGER){
        //指纹
        val imageData = blob.getStImageBytes
        if(imageData.size() > 0){
          val finger = new GafisGatherFinger()
          finger.personId = personId
          finger.gatherData = imageData.toByteArray
          finger.fgp = blob.getFgp.getNumber.toShort
          finger.fgpCase = if(blob.getBPlain) Gafis70Constants.FGP_CASE_PLAIN.toString else Gafis70Constants.FGP_CASE_ROLL.toString
          finger.groupId = Gafis70Constants.GROUP_ID_CPR
          finger.lobtype = Gafis70Constants.LOBTYPE_DATA
          fingerList += finger
        }
        //特征
        val mntData = blob.getStMntBytes
        if(mntData.size() >0){
          val mnt = new GafisGatherFinger()
          mnt.personId = personId
          mnt.gatherData = mntData.toByteArray
          mnt.fgp = blob.getFgp.getNumber.toShort
          mnt.fgpCase = if(blob.getBPlain) Gafis70Constants.FGP_CASE_PLAIN.toString else Gafis70Constants.FGP_CASE_ROLL.toString
          mnt.groupId = Gafis70Constants.GROUP_ID_MNT
          mnt.lobtype = Gafis70Constants.LOBTYPE_MNT
          fingerList += mnt
        }
        //纹线
        val binData = blob.getStBinBytes
        if(binData.size() >0){
          val bin = new GafisGatherFinger()
          bin.personId = personId
          bin.gatherData = binData.toByteArray
          bin.fgp = blob.getFgp.getNumber.toShort
          bin.fgpCase = if(blob.getBPlain) Gafis70Constants.FGP_CASE_PLAIN.toString else Gafis70Constants.FGP_CASE_ROLL.toString
          bin.groupId = Gafis70Constants.GROUP_ID_BIN
          bin.lobtype = Gafis70Constants.LOBTYPE_MNT

          fingerList += bin
        }
      }
    }

    fingerList.toSeq
  }

  /**
    * 人像转换
    *
    * @param tpCard
    * @return
    */
  def convertTPCard2GafisGatherPortrait(tpCard: TPCard): Seq[GafisGatherPortrait]={
    val portaitList = new ArrayBuffer[GafisGatherPortrait]()
    val personId = tpCard.getStrCardID
    val blobIter = tpCard.getBlobList.iterator()
    while (blobIter.hasNext){
      val blob = blobIter.next()
      if(blob.getType == ImageType.IMAGETYPE_FACE && blob.getStImageBytes.size() > 0){
        val portrait = new GafisGatherPortrait()
        //指纹系统人像字典对应
        portrait.fgp = blob.getFacefgp match {
          case FaceFgp.FACE_FRONT =>
            Gafis70Constants.FACE_FRONT
          case FaceFgp.FACE_RIGHT =>
            Gafis70Constants.FACE_RIGHT
          case FaceFgp.FACE_LEFT=>
            Gafis70Constants.FACE_LEFT
        }
        portrait.gatherData = blob.getStImageBytes.toByteArray
        portrait.personid = personId

        portaitList += portrait
      }
    }
    portaitList
  }

  /**
    * 掌纹转换
    *
    * @param tpCard
    * @return
    */
  def convertTPCard2GafisGatherPalm(tpCard: TPCard): Seq[GafisGatherPalm] ={
    val palmList = new ArrayBuffer[GafisGatherPalm]()
    val personId = tpCard.getStrCardID
    val blobIter = tpCard.getBlobList.iterator()
    while (blobIter.hasNext){
      val blob = blobIter.next()
      if(blob.getType == ImageType.IMAGETYPE_PALM){
        if( blob.getStImageBytes.size() > 0){
          val palm = new GafisGatherPalm()
          palm.gatherData = blob.getStImageBytes.toByteArray
          palm.fgp = blob.getPalmfgp match {
            case PalmFgp.PALM_RIGHT => Gafis70Constants.PALM_RIGHT
            case PalmFgp.PALM_LEFT => Gafis70Constants.PALM_LEFT
            case PalmFgp.PALM_FINGER_R => Gafis70Constants.PALM_FINGER_R
            case PalmFgp.PALM_FINGER_L => Gafis70Constants.PALM_FINGER_L
            case PalmFgp.PALM_THUMB_R_LOW => Gafis70Constants.PALM_THUMB_R_LOW
            case PalmFgp.PALM_THUMB_R_UP => Gafis70Constants.PALM_THUMB_R_UP
            case PalmFgp.PALM_THUMB_L_LOW => Gafis70Constants.PALM_THUMB_L_LOW
            case PalmFgp.PALM_THUMB_L_UP => Gafis70Constants.PALM_THUMB_L_UP
            case PalmFgp.PALM_UNKNOWN => 0
          }
          palm.personId = personId
          palm.groupId = Gafis70Constants.GROUP_ID_CPR
          palm.lobtype = Gafis70Constants.LOBTYPE_DATA

          palmList += palm
        }
        //特征
        val mntData = blob.getStMntBytes
        if(mntData.size() >0){
          val mnt = new GafisGatherPalm()
          mnt.personId = personId
          mnt.gatherData = mntData.toByteArray
          mnt.fgp = blob.getPalmfgp match {
            case PalmFgp.PALM_RIGHT => 11
            case PalmFgp.PALM_LEFT => 12
            case PalmFgp.PALM_UNKNOWN => 0
          }
          mnt.groupId = Gafis70Constants.GROUP_ID_MNT
          mnt.lobtype = Gafis70Constants.LOBTYPE_MNT

          palmList += mnt
        }
        //纹线
        val binData = blob.getStBinBytes
        if(binData.size() >0){
          val bin = new GafisGatherPalm()
          bin.personId = personId
          bin.gatherData = binData.toByteArray
          bin.fgp = blob.getPalmfgp match{
            case PalmFgp.PALM_RIGHT => 11
            case PalmFgp.PALM_LEFT => 12
            case PalmFgp.PALM_UNKNOWN => 0
          }
          bin.groupId = Gafis70Constants.GROUP_ID_BIN
          bin.lobtype = Gafis70Constants.LOBTYPE_MNT

          palmList += bin
        }
      }
    }
    palmList
  }

  def convertMatchTask2GafisNormalqueryQueryque(matchTask: MatchTask): GafisNormalqueryQueryque = {
    val gafisQuery = new GafisNormalqueryQueryque()
    gafisQuery.keyid = matchTask.getMatchId

    gafisQuery.priority = matchTask.getPriority.toShort
    gafisQuery.prioritynew = matchTask.getPriority.toShort
    gafisQuery.minscore = matchTask.getScoreThreshold.toLong
    gafisQuery.maxcandnum = matchTask.getTopN.toLong
    gafisQuery.queryid = matchTask.getObjectId //记录查询任务号
    gafisQuery.status = QueryConstants.STATUS_WAIT
    gafisQuery.username = matchTask.getCommitUser

    matchTask.getMatchType match {
      case MatchType.FINGER_LL |
           MatchType.FINGER_LT |
           MatchType.FINGER_TL |
           MatchType.FINGER_TT =>
        gafisQuery.flag = QueryConstants.FLAG_FINGER
      case MatchType.PALM_LL |
           MatchType.PALM_LT |
           MatchType.PALM_TL |
           MatchType.PALM_TT =>
        gafisQuery.flag = QueryConstants.FLAG_PALM
    }
    if (gafisQuery.flag == QueryConstants.FLAG_PALM) {
      gafisQuery.querytype = (matchTask.getMatchType.ordinal() - 4).toShort
    } else {
      gafisQuery.querytype = matchTask.getMatchType.ordinal().toShort
    }
    val ga = new galocpkg with gitempkg with galoctp{}
    //特征mic，现场只有一枚指纹或者掌纹，捺印有多枚指纹或掌纹
    matchTask.getMatchType match {
      case MatchType.FINGER_LL |
           MatchType.FINGER_LT |
           MatchType.PALM_LL |
           MatchType.PALM_LT=>
        val mic = new GAFISMICSTRUCT
        mic.bIsLatent = 1
        mic.pstMnt_Data = matchTask.getLData.getMinutia.toByteArray
        mic.nMntLen = mic.pstMnt_Data.length
        mic.nItemFlag = glocdef.GAMIC_ITEMFLAG_MNT.asInstanceOf[Byte]
        mic.nItemData = 1
        if (gafisQuery.flag == QueryConstants.FLAG_PALM || gafisQuery.flag == QueryConstants.FLAG_PALM_TEXT) {
          mic.nItemType = glocdef.GAMIC_ITEMTYPE_PALM.asInstanceOf[Byte]
        } else {
          mic.nItemType = glocdef.GAMIC_ITEMTYPE_FINGER.asInstanceOf[Byte]
        }

        val micBuf = ChannelBuffers.buffer(ga.GAFIS_MIC_MicStreamLen(Array(mic)))
        ga.GAFIS_MIC_Mic2Stream(mic, micBuf)
        gafisQuery.mic = micBuf.array()
      case MatchType.FINGER_TT |
           MatchType.FINGER_TL |
           MatchType.PALM_TT |
           MatchType.PALM_TL=>
        var mics = new ArrayBuffer[GAFISMICSTRUCT]
        matchTask.getTData.getMinutiaDataList.foreach{ md =>
          val mic = new GAFISMICSTRUCT
          mic.pstMnt_Data = md.getMinutia.toByteArray
          mic.nMntLen = mic.pstMnt_Data.length
          mic.nItemFlag = glocdef.GAMIC_ITEMFLAG_MNT.asInstanceOf[Byte]
          mic.nItemData =  md.getPos.toByte
          if (gafisQuery.flag == QueryConstants.FLAG_PALM || gafisQuery.flag == QueryConstants.FLAG_PALM_TEXT) {
            mic.nItemType = glocdef.GAMIC_ITEMTYPE_PALM.asInstanceOf[Byte]
          } else {
            mic.nItemType = glocdef.GAMIC_ITEMTYPE_FINGER.asInstanceOf[Byte]
          }
          mics += mic
        }
        val micBuf = ChannelBuffers.buffer(ga.GAFIS_MIC_MicStreamLen(mics.toArray))
        ga.GAFIS_MIC_MicArray2Stream(mics.toArray, micBuf)
        gafisQuery.mic = micBuf.array()
    }

    //TODO textsql 文本
    gafisQuery
  }

  def convertGafisNormalqueryQueryque2MatchTask(gafisQuery: GafisNormalqueryQueryque): MatchTask = {
    val matchTask = MatchTask.newBuilder()
    matchTask.setMatchId(gafisQuery.keyid)
    matchTask.setObjectId(gafisQuery.oraSid)//必填项，现在用于存放oraSid
    matchTask.setPriority(gafisQuery.priority.toInt)
    matchTask.setScoreThreshold(gafisQuery.minscore.toInt)
    matchTask.setTopN(gafisQuery.maxcandnum.toInt)
    if(null!=gafisQuery.status)matchTask.setStatus(gafisQuery.status.toInt)
    if(null!=gafisQuery.username) matchTask.setCommitUser(gafisQuery.username)
    matchTask.setComputerIp(gafisQuery.computerip)
    if(null!=gafisQuery.userunitcode) matchTask.setUserUnitCode(gafisQuery.userunitcode)
    matchTask.setOraCreatetime(DateConverter.convertDate2String(gafisQuery.createtime, "yyyyMMddHHmmss"))
    val flag = gafisQuery.flag
    val isPalm = flag == QueryConstants.FLAG_PALM || flag == QueryConstants.FLAG_PALM_TEXT
    val matchType = convertQueryType2MatchType(gafisQuery.querytype, isPalm)
    matchTask.setMatchType(matchType)
    val mics = new galoctp{}.GAFIS_MIC_GetDataFromStream(ChannelBuffers.wrappedBuffer(gafisQuery.mic))
    mics.foreach{mic =>
      if(mic.bIsLatent == 1){
        val ldata = LatentMatchData.newBuilder()
        ldata.setMinutia(ByteString.copyFrom(mic.pstMnt_Data))
        if(mic.pstBin_Data != null){
          ldata.setRidge(ByteString.copyFrom(mic.pstBin_Data))
        }
        matchTask.setLData(ldata)
      }else{
        val tdata = matchTask.getTDataBuilder.addMinutiaDataBuilder()
        mic.nItemType match {
          case glocdef.GAMIC_ITEMTYPE_FINGER =>
            tdata.setMinutia(ByteString.copyFrom(mic.pstMnt_Data)).setPos(mic.nItemData)
          case glocdef.GAMIC_ITEMTYPE_TPLAIN =>
            //平面
            tdata.setMinutia(ByteString.copyFrom(mic.pstMnt_Data)).setPos(mic.nItemData)
          case glocdef.GAMIC_ITEMTYPE_PALM =>
            //掌纹
            tdata.setMinutia(ByteString.copyFrom(mic.pstMnt_Data)).setPos(mic.nItemFlag)
          case other =>
            warn("unsupport itemType:", other)
        }
        if(mic.pstBin_Data != null){
          tdata.setRidge(ByteString.copyFrom(mic.pstBin_Data))
        }
      }
    }
    matchTask.build()
  }

  def convertMatchResult2GafisNormalqueryQueryque(matchResult: MatchResult, gafisQuery: GafisNormalqueryQueryque = new GafisNormalqueryQueryque()): GafisNormalqueryQueryque ={
    gafisQuery.curcandnum = matchResult.getCandidateNum.toLong
    gafisQuery.recordNumMatched = matchResult.getRecordNumMatched
    /*if(gafisQuery.querytype != 0){//如果不是TT查询，查中概率=最大分数/10
      gafisQuery.hitpossibility = (matchResult.getMaxScore /10).toShort
    }else{
      gafisQuery.hitpossibility = matchResult.getMaxScore.toShort
    }*/

    gafisQuery.hitpossibility = matchResult.getHITPOSSIBILITY.toShort

    //如果有候选队列，处理状态为待处理0,比中状态0;否则已处理1,没有比中1
    if(matchResult.getCandidateNum > 0){
      gafisQuery.verifyresult = 0.toShort
      gafisQuery.handleresult = 0.toShort
    }else{
      gafisQuery.verifyresult = 99.toShort
      gafisQuery.handleresult = 1.toShort
    }

    gafisQuery.candlist = convertMatchResult2CandList(matchResult)

    gafisQuery
  }


  def convertGafisCheckInfo2MatchSysInfo(gafisCheckinInfo:GafisCheckinInfo):MatchRelationInfo = {
    val matchRelationInfo = MatchRelationInfo.newBuilder
    val isPlam = !(gafisCheckinInfo.cardType1==1)
    val queryType = gafisCheckinInfo.querytype
    if(queryType == QueryConstants.QUERY_TYPE_TT || queryType == QueryConstants.QUERY_TYPE_LL){
      matchRelationInfo.setSrckey(gafisCheckinInfo.code)
      matchRelationInfo.setDestkey(gafisCheckinInfo.tcode)
    }else if(queryType == QueryConstants.QUERY_TYPE_TL){
      matchRelationInfo.setSrckey(gafisCheckinInfo.tcode)
      matchRelationInfo.setDestkey(gafisCheckinInfo.code)
    }else if(queryType == QueryConstants.QUERY_TYPE_LT){
      matchRelationInfo.setSrckey(gafisCheckinInfo.code)
      matchRelationInfo.setDestkey(gafisCheckinInfo.tcode)
    }else{
      throw new Exception("queryType error:" + queryType)
    }
    matchRelationInfo.setQuerytype(queryType)
    matchRelationInfo.setQueryTaskId(GafisNormalqueryQueryque.find_by_pkId(gafisCheckinInfo.queryUuid).headOption.get.queryid.toString)
    matchRelationInfo.setInputer(gafisCheckinInfo.registerUser)
    matchRelationInfo.setInputDate(DateConverter.convertDate2String(gafisCheckinInfo.registerTime,"yyyyMMddHHmmss"))
    matchRelationInfo.setInputUnitCode(gafisCheckinInfo.registerOrg)
    matchRelationInfo.setScore(gafisCheckinInfo.fraction.toString)
    matchRelationInfo.setRank(gafisCheckinInfo.rank.toString)
    matchRelationInfo.setFirstRankScore(gafisCheckinInfo.fraction.toString)
    matchRelationInfo.setFg(FingerFgp.valueOf(Integer.valueOf(gafisCheckinInfo.fgp)))
    matchRelationInfo.setHitposs(gafisCheckinInfo.hitpossibility.toInt)
    matchRelationInfo.setBreakUsername(gafisCheckinInfo.confirmUser)
    matchRelationInfo.setBreakDateTime(DateConverter.convertDate2String(gafisCheckinInfo.confirmTime,"yyyyMMddHHmmss"))
    matchRelationInfo.setBreakUserUnitCode(gafisCheckinInfo.registerOrg)
    matchRelationInfo.setBreakid(CommonUtils.createBreakId(gafisCheckinInfo.confirmUser,gafisCheckinInfo.confirmTime,gafisCheckinInfo.rank))
    matchRelationInfo.setMatchUnitCode(gafisCheckinInfo.reviewOrg)
    matchRelationInfo.setMatcher(gafisCheckinInfo.confirmUser)
    matchRelationInfo.setMatchDate(DateConverter.convertDate2String(gafisCheckinInfo.confirmTime,"yyyyMMddHHmmss"))
    matchRelationInfo.build
  }

  private def convertMatchResult2CandList(matchResult: MatchResult): Array[Byte] = {
    val result = new ByteArrayOutputStream()
    val queryType = 0
    val candIter = matchResult.getCandidateResultList.iterator()
    var index = 0//比对排名
    while(candIter.hasNext){
      index += 1
      val cand = candIter.next()
      result.write(new Array[Byte](4))
      result.write(CommonUtils.int2Byte(cand.getScore))
      result.write(cand.getObjectId.getBytes())
      result.write(new Array[Byte](32 - cand.getObjectId.getBytes().length + 2))
      val dbId = if (queryType == 0 || queryType == 2) 1 else 2
      result.write(ByteBuffer.allocate(2).putShort(dbId.toShort).array())
      result.write(ByteBuffer.allocate(2).putShort(2.toShort).array())
      result.write(new Array[Byte](2 + 1 + 3 + 1 + 1 + 1 + 1))
      result.write(cand.getPos.toByte)
      result.write(new Array[Byte](1 + 2 + 1 + 1 + 1 + 1))
      result.write(CommonUtils.getAFISDateTime(new Date()))
      result.write(new Array[Byte](2 + 2 + 2 + 2))
      result.write(new Array[Byte](8+2))
      result.write(CommonUtils.int2Byte(index))
      result.write(new Array[Byte](2))
    }

    result.toByteArray
  }

  def convertQueryType2MatchType(queryType: Short, isPalm: Boolean): MatchType={
    if(isPalm){
      queryType match {
        case QueryConstants.QUERY_TYPE_TT =>
          MatchType.PALM_TT
        case QueryConstants.QUERY_TYPE_TL =>
          MatchType.PALM_TL
        case QueryConstants.QUERY_TYPE_LT =>
          MatchType.PALM_LT
        case QueryConstants.QUERY_TYPE_LL =>
          MatchType.PALM_LL
        case other =>
          throw new IllegalArgumentException("unknown queryType:"+ queryType)
      }
    }else{
      queryType match {
        case QueryConstants.QUERY_TYPE_TT =>
          MatchType.FINGER_TT
        case QueryConstants.QUERY_TYPE_TL =>
          MatchType.FINGER_TL
        case QueryConstants.QUERY_TYPE_LT =>
          MatchType.FINGER_LT
        case QueryConstants.QUERY_TYPE_LL =>
          MatchType.FINGER_LL
        case other =>
          throw new IllegalArgumentException("unknown queryType:"+ queryType)
      }
    }
  }

  private def getCode7to6(map:Map[String,String],code:String): String ={
    if(map.keys.toList.contains(code)){
      map.get(code).get
    }else{
      code
    }
  }
}
