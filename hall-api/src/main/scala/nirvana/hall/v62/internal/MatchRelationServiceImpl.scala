package nirvana.hall.v62.internal


import javax.sql.DataSource

import monad.support.services.LoggerSupport
import nirvana.hall.api.internal.DateConverter
import nirvana.hall.api.services.{LPCardService, MatchRelationService, QueryService}
import nirvana.hall.c.services.gloclib.galoclog.GAFIS_VERIFYLOGSTRUCT
import nirvana.hall.c.services.gloclib.galoctp._
import nirvana.hall.protocol.api.FPTProto.{FingerFgp, MatchRelationInfo}
import nirvana.hall.protocol.api.HallMatchRelationProto.{MatchRelationGetRequest, MatchRelationGetResponse, MatchStatus}
import nirvana.hall.protocol.fpt.MatchRelationProto.{MatchRelation, MatchRelationTLAndLT, MatchRelationTT, MatchSysInfo}
import nirvana.hall.protocol.matcher.NirvanaTypeDefinition.MatchType
import nirvana.hall.v62.config.HallV62Config
import nirvana.hall.v62.internal.c.gloclib.ganetlogverifyConverter
import nirvana.hall.v62.internal.c.gloclib.gcolnames._
import org.apache.tapestry5.ioc.internal.util.InternalUtils

/**
 * Created by songpeng on 16/6/21.
 */
class MatchRelationServiceImpl(v62Config: HallV62Config, facade: V62Facade, lPCardService: LPCardService, queryService: QueryService,implicit val dataSource: DataSource) extends MatchRelationService with LoggerSupport{
  /**
   * 获取比对关系
    *
    * @param request
   * @return
   */
  override def getMatchRelation(request: MatchRelationGetRequest): MatchRelationGetResponse = {
    val matchType = request.getMatchType
    val response = MatchRelationGetResponse.newBuilder()
    response.setMatchType(matchType)
    val cardId = request.getCardId
    //获取比对状态
    val status = queryService.findFirstQueryStatusByCardIdAndMatchType(cardId, matchType)
    response.setMatchStatus(status)
    matchType match {
      case MatchType.FINGER_TL =>
        if(status == MatchStatus.RECHECKED){//倒查复核后生成比对关系
        //倒查直接查询比中关系表
        val statement = Option("(SrcKey='%s')".format(cardId))
          val matchInfo = queryMatchInfo(statement, 1)
          matchInfo.foreach{ verifyLog: GAFIS_VERIFYLOGSTRUCT=>
            val matchRelationTL = MatchRelationTLAndLT.newBuilder()
            matchRelationTL.setPersonId(verifyLog.szSrcKey)
            matchRelationTL.setFpg(FingerFgp.valueOf(verifyLog.nFg))
            matchRelationTL.setCapture(verifyLog.bIsCrimeCaptured > 0)

            //根据现场卡号查询案件卡号，seqno信息
            val fingerId = verifyLog.szDestKey
            val lpCard = lPCardService.getLPCard(fingerId)
            val caseId = lpCard.getText.getStrCaseId
            matchRelationTL.setCaseId(caseId)
            matchRelationTL.setSeqNo(lpCard.getText.getStrSeq)

            val matchSysInfo = MatchSysInfo.newBuilder()
            matchSysInfo.setMatchUnitCode(verifyLog.szSubmitUserUnitCode)
            matchSysInfo.setMatchUnitName("")
            matchSysInfo.setMatcher(verifyLog.szSubmitUserName)
            matchSysInfo.setMatchDate(DateConverter.convertAFISDateTime2String(verifyLog.tSubmitDateTime).substring(0, 8))
            matchSysInfo.setInputer(verifyLog.szBreakUserName)
            matchSysInfo.setInputUnitCode(verifyLog.szBreakUserUnitCode)
            matchSysInfo.setInputDate(DateConverter.convertAFISDateTime2String(verifyLog.tBreakDateTime).substring(0, 8))
            matchSysInfo.setApprover(verifyLog.szReCheckUserName)
            matchSysInfo.setApproveDate(DateConverter.convertAFISDateTime2String(verifyLog.tReCheckDateTime).substring(0, 8))

            val matchRelation = MatchRelation.newBuilder()
            matchRelation.setMatchSysInfo(matchSysInfo)
            matchRelation.setExtension(MatchRelationTLAndLT.data, matchRelationTL.build())

            response.addMatchRelation(matchRelation.build())
          }
        }
      case MatchType.FINGER_TT =>
        //查重核查完毕后生成比对关系
        if(status.getNumber == MatchStatus.CHECKED.getNumber){
          //重卡信息先从捺印表查到重卡组号，然后根据重卡组号查询重卡信息
          val tp = new GTPCARDINFOSTRUCT
          facade.NET_GAFIS_FLIB_Get(v62Config.templateTable.dbId.toShort, v62Config.templateTable.tableId.toShort, cardId, tp, null, 3)
          val personId = tp.stAdmData.szPersonID.trim //重卡组号
          //如果没有重卡组号不获取重卡信息
          if (InternalUtils.isNonBlank(personId)){
            val m_stPersonInfo = new GPERSONINFOSTRUCT
            m_stPersonInfo.szPersonID = personId
            m_stPersonInfo.nItemFlag = (GPIS_ITEMFLAG_CARDCOUNT | GPIS_ITEMFLAG_CARDID | GPIS_ITEMFLAG_TEXT).toByte
            m_stPersonInfo.nItemFlag2 = (GPIS_ITEMFLAG2_LPGROUPDBID | GPIS_ITEMFLAG2_LPGROUPTID | GPIS_ITEMFLAG2_FLAG).toByte
            val dbId: Short = 1
            val tableId: Short = 3
            facade.NET_GAFIS_PERSON_Get(dbId, tableId, m_stPersonInfo)
            m_stPersonInfo.pstID_Data.foreach{personInfo =>
              if(!cardId.equals(personInfo.szCardID)){//排除自己
                val tt = MatchRelationTT.newBuilder()
                tt.setPersonId1(cardId)
                tt.setPersonId2(personInfo.szCardID)
                //TT没有单位信息
                val matchSysInfo = MatchSysInfo.newBuilder()
                matchSysInfo.setMatchUnitCode("")
                matchSysInfo.setMatchUnitName("")
                matchSysInfo.setMatcher(personInfo.szUserName)
                matchSysInfo.setMatchDate(DateConverter.convertAFISDateTime2String(personInfo.tCheckTime).substring(0, 8))
                val matchRelation = MatchRelation.newBuilder()
                matchRelation.setMatchSysInfo(matchSysInfo)
                matchRelation.setExtension(MatchRelationTT.data, tt.build())

                response.addMatchRelation(matchRelation.build())
              }
            }
          }
        }
      case other =>
        warn("unsupport matchType:{}", matchType)
    }

    response.build()
  }
  /**
   * 查询比中关系
   *
   * @param statementOpt 查询语句
   * @param limit 抓取多少条
   * @return 查询结构
   */
  private def queryMatchInfo(statementOpt:Option[String],limit:Int): List[GAFIS_VERIFYLOGSTRUCT]={
    val mapper = Map(
      g_stCN.stBc.pszBreakID->"szBreakID",
      g_stCN.stBc.pszSrcKey->"szSrcKey",
      g_stCN.stBc.pszDestKey->"szDestKey",
      g_stCN.stBc.pszScore->"nScore",
      g_stCN.stBc.pszFirstRankScore->"nFirstRankScore",
      g_stCN.stBc.pszRank->"nRank",
      g_stCN.stBc.pszFg->"nFg",
      g_stCN.stBc.pszHitPoss->"nHitPoss",
      g_stCN.stBc.pszIsRemoteSearched->"bIsRmtSearched",
      g_stCN.stBc.pszIsCrimeCaptured->"bIsCrimeCaptured",
      g_stCN.stBc.pszTotoalMatchedCnt->"nTotalMatchedCnt",
      g_stCN.stBc.pszQueryType->"nQueryType",
      g_stCN.stBc.pszSrcDBID->"nSrcDBID",
      g_stCN.stBc.pszDestDBID->"nDestDBID",
      g_stCN.stBc.pszSrcPersonCaseID->"szSrcPersonCaseID",
      g_stCN.stBc.pszDestPersonCaseID->"szDestPersonCaseID",
      g_stCN.stBc.pszCaseClassCode1->"szCaseClassCode1",
      g_stCN.stBc.pszCaseClassCode2->"szCaseClassCode2",
      g_stCN.stBc.pszCaseClassCode3->"szCaseClassCode3",
      g_stCN.stBc.pszSubmitUserName->"szSubmitUserName",
      g_stCN.stBc.pszSubmitUserUnitCode->"szSubmitUserUnitCode",
      g_stCN.stBc.pszSubmitDateTime->"tSubmitDateTime",
      g_stCN.stBc.pszBreakUserName->"szBreakUserName",
      g_stCN.stBc.pszBreakUserUnitCode->"szBreakUserUnitCode",
      g_stCN.stBc.pszBreakDateTime->"tBreakDateTime",
      g_stCN.stBc.pszReCheckUserName->"szReCheckUserName",
      g_stCN.stBc.pszReCheckerUnitCode->"szReCheckerUnitCode",
      g_stCN.stBc.pszReCheckDate->"tReCheckDateTime",
      g_stCN.stNuminaCol.pszSID->"nSID"
    )

    val dbDef = facade.NET_GAFIS_MISC_GetDefDBID()
    facade.queryV62Table[GAFIS_VERIFYLOGSTRUCT](
      dbDef.nAdminDefDBID,
      g_stCN.stSysAdm.nTIDBreakCaseTable,
      mapper,
      statementOpt,
      limit)
  }

  /**
    * 增加比中关系
    *
    * @param matchRelationInfo
    * @param dbId
    */
  override def addMatchRelation(matchRelationInfo: MatchRelationInfo, dbId: Option[String]): Unit = {
    val gafis_VERIFYLOGSTRUCT = ganetlogverifyConverter.convertProtoBuf2GAFIS_VERIFYLOGSTRUCT(matchRelationInfo)
    facade.NET_GAFIS_VERIFYLOG_Add(gafis_VERIFYLOGSTRUCT)
  }

  /**
    * 判断是否存在该breakid的比中关系
    *
    * @param szBreakID
    * @param dbId
    * @return
    */
  override def isExist(szBreakID: String, dbId: Option[String]): Boolean = {
    var bStr = false
    val statement = Option("(BREAKID='%s')".format(szBreakID))
    bStr = queryMatchInfo(statement, 1).size > 0
    bStr
  }

  /**
    * 更新比中关系
    *
    * @param matchRelationInfo
    * @param dbId
    */
  override def updateMatchRelation(matchRelationInfo: MatchRelationInfo, dbId: Option[String]): Unit = {
    val gafis_VERIFYLOGSTRUCT = ganetlogverifyConverter.convertProtoBuf2GAFIS_VERIFYLOGSTRUCT(matchRelationInfo)
    facade.NET_GAFIS_VERIFYLOG_Update(gafis_VERIFYLOGSTRUCT)
  }

  /**
    * 获取DBID
    *
    * @param dbId
    */
  private def getDBID(dbId: Option[String]): Short ={
    if(dbId == None){
      v62Config.breakTable.dbId.toShort
    }else{
      dbId.get.toShort
    }
  }

  /**
    * 获取比对关系
    *
    * @param breakId
    * @return
    */
  override def getMatchRelation(breakId: String): MatchRelationInfo = ???
}