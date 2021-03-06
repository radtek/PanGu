package nirvana.hall.v70.internal.sync

import java.io.ByteArrayOutputStream
import java.sql.Timestamp
import java.util.UUID
import javax.sql.DataSource

import nirvana.hall.api.config.QueryQue
import nirvana.hall.api.internal.DateConverter
import nirvana.hall.api.jpa.HallFetchConfig
import nirvana.hall.api.services.sync.FetchQueryService
import nirvana.hall.c.services.gloclib.gaqryque.GAQUERYCANDSTRUCT
import nirvana.hall.protocol.matcher.MatchResultProto.MatchResult
import nirvana.hall.protocol.matcher.MatchResultProto.MatchResult.MatcherStatus
import nirvana.hall.protocol.matcher.MatchTaskQueryProto.MatchTask
import nirvana.hall.support.services.JdbcDatabase
import nirvana.hall.v62.internal.c.gloclib.gaqryqueConverter
import nirvana.hall.v70.internal.adapter.common.jpa.{GafisNormalqueryQueryque}
import nirvana.hall.v70.jpa.{GafisTask62Record, HallReadRecord}
import org.apache.commons.lang.StringUtils

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by songpeng on 16/8/26.
  */
class FetchQueryServiceImpl(implicit datasource: DataSource) extends FetchQueryService{
  /**
    * 获取查询任务
 *
    * @param size
    * @return
    */
  override def fetchMatchTask(size: Int,yearThreshold:String, dbId: Option[String]): Seq[MatchTask] = {

    val matchTaskList = new ArrayBuffer[MatchTask]

    val sql = new StringBuilder;
    sql ++= s"SELECT * FROM (SELECT " +
      s"t.username " +
      s",t.computerip" +
      s",t.userunitcode" +
      s",t.ora_sid" +
      s",t.createtime" +
      s",t.keyid" +
      s",t.minscore" +
      s",t.querytype" +
      s",t.priority" +
      s",t.maxcandnum" +
      s",t.mic" +
      s",t.qrycondition" +
      s",t.textsql" +
      s",t.flag " +
      s",t.status " +
    s" FROM Gafis_Normalquery_Queryque  t " +
      s"WHERE  NOT EXISTS (SELECT 1 " +
      s"FROM HALL_READ_RECORD p " +
      s"WHERE p.orasid=t.ora_sid) AND t.SYNC_TARGET_SID IS NULL  AND t.submittsystem <> 3 AND t.status = 2"
    if(StringUtils.isNotEmpty(yearThreshold) && StringUtils.isNotBlank(yearThreshold)){
      sql ++= s"  AND t.createtime>= to_date('"+ yearThreshold +"','yyyy-mm-dd hh24:mi:ss')"
    }
    sql ++= s" ORDER BY t.createtime ASC ) WHERE  ROWNUM <=?"
    JdbcDatabase.queryWithPsSetter(sql.toString) {ps =>
      ps.setLong(1, size)
    } { rs =>
      val gaQuery = new GafisNormalqueryQueryque()
      gaQuery.oraSid = rs.getLong("ora_sid")
      gaQuery.keyid = rs.getString("keyid")
      gaQuery.minscore = rs.getInt("minscore").toLong
      gaQuery.querytype = rs.getShort("querytype")
      gaQuery.priority = rs.getShort("priority")
      gaQuery.maxcandnum = rs.getInt("maxcandnum").toLong
      gaQuery.flag = rs.getShort("flag")
      gaQuery.mic = rs.getBytes("mic")
      gaQuery.createtime = rs.getTimestamp("createtime")
      gaQuery.username = rs.getString("username")
      gaQuery.computerip = rs.getString("computerip")
      gaQuery.userunitcode = rs.getString("userunitcode")
      gaQuery.status = rs.getShort("status")
      matchTaskList += ProtobufConverter.convertGafisNormalqueryQueryque2MatchTask(gaQuery)
    }

    matchTaskList.toSeq




  }

  /**
    * 保存候选信息
 *
    * @param matchResult
    */
  override def saveMatchResult(matchResult: MatchResult, fetchConfig: HallFetchConfig, candDBIDMap: Map[String, Short] = Map()) = {

    val sql = s"UPDATE GAFIS_NORMALQUERY_QUERYQUE t " +
              s"SET t.status= 2" +
                  s", t.curcandnum=?" +
                  s", t.candlist=?" +
                  s", t.hitpossibility=?" +
                  s", t.verifyresult=?" +
                  s", t.handleresult=?" +
                  s", t.time_elapsed=?" +
                  s", t.match_progress=100" +
                  s", t.FINISHTIME=? " +
              s"WHERE t.queryid=?"
    val oraSid = matchResult.getMatchId
    val candNum = matchResult.getCandidateNum
    var maxScore = matchResult.getMaxScore
    val queryQue = getQueryQue(oraSid)
    val candHead = getCandHead(matchResult, queryQue)
    var candList:Array[Byte] = null
    if(candNum > 0){
      candList = convertMatchResult2CandList(matchResult, queryQue.queryType)
    }
    if (queryQue.queryType != 0) {
      maxScore = maxScore / 10
    }
    JdbcDatabase.update(sql) { ps =>
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
      ps.setLong(6, matchResult.getTimeElapsed * 1000L)
      ps.setTimestamp(7,new Timestamp(DateConverter.convertString2Date(matchResult.getMatchFinishTime,"yyyyMMddHHmmss").getTime))
      ps.setString(8, oraSid)
    }
  }

  /**
    * 根据远程查询queryid获取查询结果信息
    *
    * @param queryId
    * @param pkId
    * @param typ
    * @param dbId
    * @return
    */
  override def getMatchResultByQueryid(queryId: Long, pkId: String, typ: Short, dbId: Option[String]): Option[MatchResult] = {
    val matchResult = MatchResult.newBuilder()
    matchResult.setMatchId(queryId.toString)
    matchResult.setStatus(MatcherStatus.newBuilder().setCode(0))
    val queryQueOpt = GafisNormalqueryQueryque.find_by_queryid_and_keyid_and_querytype(queryId, pkId, typ).headOption
    if(queryQueOpt.nonEmpty){
      val queryQue = queryQueOpt.get
      if(queryQue.status >= 2){
        if(queryQue.candlist != null){
          val candResultObj = gaqryqueConverter.convertCandList2MatchResultObject(queryQue.candlist)
          candResultObj.foreach{cand=>
            matchResult.addCandidateResult(cand)
          }
        }
        matchResult.setCandidateNum(queryQue.curcandnum.toInt)
        matchResult.setTimeElapsed(queryQue.timeElapsed)
        matchResult.setRecordNumMatched(queryQue.recordNumMatched)
        matchResult.setMaxScore(queryQue.hitpossibility.toInt)
        matchResult.setMaxcandnum(queryQue.maxcandnum.toLong) //最大候选数量
        return Option(matchResult.build())
      }
    }
    None
  }

  /**
    * 候选列表转换
 *
    * @param matchResult
    * @param queryType
    * @return
    */
  def convertMatchResult2CandList(matchResult: MatchResult, queryType: Int): Array[Byte] ={
    val result = new ByteArrayOutputStream()
    val candIter = matchResult.getCandidateResultList.iterator()
    var index = 0 //比对排名
    while (candIter.hasNext) {
      index += 1
      val cand = candIter.next()
      val gCand = new GAQUERYCANDSTRUCT
      gCand.nScore = cand.getScore
      gCand.szKey = cand.getObjectId
      gCand.nDBID = if (queryType == 0 || queryType == 1) 1 else 2
      gCand.nTableID = 2
      gCand.nIndex = cand.getPos.toByte
      gCand.tFinishTime = DateConverter.convertString2AFISDateTime(cand.getMatchFinishTime)
      gCand.nStepOneRank = index
      result.write(gCand.toByteArray())
    }
    result.toByteArray
  }
  override def getQueryQue(oraSid: Int): QueryQue = {
    val sql = "select t.keyid, t.querytype, t.flag from GAFIS_NORMALQUERY_QUERYQUE t where t.ORA_SID =?"
    JdbcDatabase.queryFirst(sql) { ps =>
      ps.setInt(1, oraSid)
    } { rs =>
      val keyId = rs.getString("keyid")
      val queryType = rs.getInt("querytype")
      val flag = rs.getInt("flag")
      new QueryQue(keyId, oraSid, queryType, if(flag == 2 || flag == 22) true else false)
    }.get
  }

  /**
    * 保存抓取记录
    *
    * @author yuchen
    * @param oraSid
    */
  override def saveFetchRecord(oraSid:String) = {
    new HallReadRecord(oraSid).save
  }

  /**
    * 获得没有同步候选的比对任务的任务号
    *
    * @author yuchen
    * @param size 单次请求数量
    */
  override def getTaskNumWithNotSyncCandList(size: Int): ListBuffer[mutable.HashMap[String,Any]] = {
    val sql = s"SELECT " +
                s"t.uuid" +
                s",t.queryid" +
                s",t.orasid" +
                s",t.querytype" +
                s",t.keyid " +
              s"FROM Gafis_Task62Record t " +
              s"WHERE t.issynccandlist = '0' AND ROWNUM <=?"
    val resultList = new mutable.ListBuffer[mutable.HashMap[String,Any]]
    JdbcDatabase.queryWithPsSetter(sql) { ps =>
      ps.setInt(1,size)
    } { rs =>
      var map = new scala.collection.mutable.HashMap[String,Any]
      map += ("uuid" -> rs.getString("uuid"))
      map += ("queryid" -> rs.getString("queryid"))
      map += ("orasid" -> rs.getString("orasid"))
      map += ("querytype" -> rs.getString("querytype"))
      map += ("keyid" -> rs.getString("keyid"))
      resultList.append(map)
    }
    resultList
  }

  /**
    * 更新Gafis_Task62Record的是否同步的状态
    * @author yuchen
    * @param status
    * @param uuid
    */
  override def updateStatusWithGafis_Task62Record(status: String,uuid:String): Unit = {
    GafisTask62Record.update.set(isSyncCandList = status).where(GafisTask62Record.id === uuid).execute
  }

  /**
    * 7.0抓取过来的任务的信息，有了这些信息后，为了通过这些任务号再去抓取比对结果。
    */
  override def recordGafisTask(objectId:String,queryId:String,isSyncCandList:String,matchType:String,cardId:String,pkId:String): Unit = {
    new GafisTask62Record(UUID.randomUUID().toString.replace("-",""),objectId,queryId,isSyncCandList,matchType,cardId,pkId).save
  }
}
