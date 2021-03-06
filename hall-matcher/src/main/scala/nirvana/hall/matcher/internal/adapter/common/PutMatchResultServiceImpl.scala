package nirvana.hall.matcher.internal.adapter.common

import javax.sql.DataSource

import nirvana.hall.c.services.gloclib.gaqryque.GAQUERYCANDSTRUCT
import nirvana.hall.matcher.HallMatcherConstants
import nirvana.hall.matcher.internal.GafisConverter
import nirvana.hall.matcher.internal.adapter.common.vo.QueryQueVo
import nirvana.hall.matcher.service.{AutoCheckService, PutMatchResultService}
import nirvana.hall.support.services.JdbcDatabase
import nirvana.protocol.MatchResult.MatchResultRequest.MatcherStatus
import nirvana.protocol.MatchResult.MatchResultResponse.MatchResultResponseStatus
import nirvana.protocol.MatchResult.{MatchResultRequest, MatchResultResponse}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Created by songpeng on 16/4/9.
 */
class PutMatchResultServiceImpl(autoCheckService: AutoCheckService, implicit val dataSource: DataSource) extends PutMatchResultService {
  val UPDATE_MATCH_RESULT_SQL = "update GAFIS_NORMALQUERY_QUERYQUE t set t.status="+HallMatcherConstants.QUERY_STATUS_SUCCESS+", t.curcandnum=?, t.candlist=?, t.hitpossibility=?, t.verifyresult=?, t.handleresult=?, t.time_elapsed=?, t.record_num_matched=?, t.match_progress=100, t.FINISHTIME=sysdate where t.ora_sid=?"
  val GET_QUERY_QUE_SQL = "select t.keyid, t.querytype, t.flag, t.maxcandnum from GAFIS_NORMALQUERY_QUERYQUE t where t.ora_sid=?"
  val candStructLen = new GAQUERYCANDSTRUCT().toByteArray().length
  /**
   * 推送比对结果
   * @param matchResultRequest
   * @return
   */
  override def putMatchResult(matchResultRequest: MatchResultRequest): MatchResultResponse = {
    val matchResultResponse = MatchResultResponse.newBuilder()
    matchResultResponse.setStatus(MatchResultResponseStatus.OK)
    if (matchResultRequest.getStatus.getCode == 0) {
      val queryQue = getQueryQueVo(matchResultRequest.getMatchId.toInt)
      addMatchResult(matchResultRequest, queryQue)
      //如果查询类型是TT，自动重卡认定
      if(queryQue.queryType == HallMatcherConstants.QUERY_TYPE_TT){
        autoCheckService.ttAutoCheck(matchResultRequest, queryQue)
      }
    } else {
      updateMatchStatusFail(matchResultRequest.getMatchId, matchResultRequest.getStatus)
    }
    matchResultResponse.build()
  }

  private def addMatchResult(matchResultRequest: MatchResultRequest, queryQue: QueryQueVo): Unit = {
    val oraSid = matchResultRequest.getMatchId
    var candNum = matchResultRequest.getCandidateNum
    var maxScore = matchResultRequest.getMaxScore

    var candList:Array[Byte] = null
    if(candNum > 0){
      val sidKeyidMap = getCardIdSidMap(matchResultRequest, queryQue)
      candList = GafisConverter.convertMatchResultObjectList2CandList(matchResultRequest.getCandidateResultList, queryQue.queryType, sidKeyidMap, queryQue.isPalm)
      //重新计算候选个数，如果sid有匹配不上（数据被人为从数据库删除数据）
      candNum = candList.length / candStructLen
    }
    if (queryQue.queryType != HallMatcherConstants.QUERY_TYPE_TT) {
      maxScore = maxScore / 10 //TT最高分数是1000
    }

    JdbcDatabase.update(UPDATE_MATCH_RESULT_SQL) { ps =>
      ps.setInt(1, candNum)
      ps.setBytes(2, candList)
      ps.setInt(3, maxScore)
      if (candNum > 0) {
        //如果有候选队列，处理状态为待处理0,比中状态0;否则已处理1,没有比中1
        ps.setInt(4, 0)
        ps.setInt(5, 0)
      } else {
        ps.setInt(4, 99)
        ps.setInt(5, 1)
      }
      ps.setLong(6, matchResultRequest.getTimeElapsed)
      ps.setLong(7, matchResultRequest.getRecordNumMatched)
      ps.setLong(8, oraSid.toLong)
    }
  }

  private def updateMatchStatusFail(match_id: String, status: MatcherStatus) {
    val sql: String = "UPDATE GAFIS_NORMALQUERY_QUERYQUE t SET t.status="+HallMatcherConstants.QUERY_STATUS_FAIL+", t.ORACOMMENT=? WHERE t.ora_sid=?"
    JdbcDatabase.update(sql) { ps =>
      ps.setString(1, status.getMsg)
      ps.setString(2, match_id)
    }
  }

  /**
   * 根据候选列表的sid获取编号, 生成对应关系map
   * @param matchResultRequest
   * @param queryQue
   * @param dataSource
   * @return
   */
  private def getCardIdSidMap(matchResultRequest: MatchResultRequest, queryQue: QueryQueVo)(implicit dataSource: DataSource): Map[Long, String] = {
    val queryType = queryQue.queryType
    var sids = ""
    var sql = ""
    val map = mutable.Map[Long, String]()
    var i = 0
    val batchSize = 100 //每100条执行一次sql
    matchResultRequest.getCandidateResultList.foreach {cand=>
      if(cand.hasKeyId){
        map.put(cand.getObjectId, cand.getKeyId.toUpperCase())
      }else{
        sids += cand.getObjectId + ","
        i += 1
        if(i % batchSize == 0 || i == matchResultRequest.getCandidateNum){
          if (sids.lastIndexOf(",") > 0) {
            sids = sids.substring(0, sids.length - 1)
          }
          if (queryType == HallMatcherConstants.QUERY_TYPE_TT || queryType == HallMatcherConstants.QUERY_TYPE_LT) {
            sql = "select p.sid as sid, p.personid as cardid from gafis_person p where p.sid in (" + sids + ")"
          } else {
            if(queryQue.isPalm){
              sql = "select t.sid as sid, t.palm_id as cardid from gafis_case_palm t where t.sid in (" + sids + ")"
            }else{
              sql = "select t.sid as sid, t.finger_id as cardid from gafis_case_finger t where t.sid in (" + sids + ")"
            }
          }
          JdbcDatabase.queryWithPsSetter(sql) { ps =>
          } { rs =>
            map.put(rs.getLong("sid"), rs.getString("cardid"))
          }
          //清空sids
          sids = ""
        }
      }
    }
    map.toMap
  }

  private def getQueryQueVo(oraSid: Int)(implicit dataSource: DataSource): QueryQueVo = {
    JdbcDatabase.queryFirst(GET_QUERY_QUE_SQL) { ps =>
      ps.setInt(1, oraSid)
    } { rs =>
      val keyId = rs.getString("keyid")
      val queryType = rs.getInt("querytype")
      val flag = rs.getInt("flag")
      val maxcandnum = rs.getInt("maxcandnum")
      new QueryQueVo(keyId, oraSid, queryType, if(flag == 2 || flag == 22) true else false, maxcandnum)
    }.get
  }
}

