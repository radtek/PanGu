package nirvana.hall.v62.internal.sync

import java.io.ByteArrayOutputStream
import javax.sql.DataSource

import nirvana.hall.api.config.QueryQue
import nirvana.hall.api.internal.{ DateConverter}
import nirvana.hall.api.jpa.HallFetchConfig
import nirvana.hall.api.services.TPCardService
import nirvana.hall.api.services.sync.FetchQueryService
import nirvana.hall.c.services.gloclib.gaqryque.GAQUERYCANDSTRUCT
import nirvana.hall.protocol.matcher.MatchResultProto.MatchResult
import nirvana.hall.protocol.matcher.MatchTaskQueryProto.MatchTask
import nirvana.hall.support.services.JdbcDatabase
import nirvana.hall.v62.config.HallV62Config
import nirvana.hall.v62.internal.V62Facade
import nirvana.hall.v70.internal.query.QueryConstants
import nirvana.hall.v70.internal.sync.ProtobufConverter
import nirvana.hall.v70.internal.adapter.common.jpa.GafisNormalqueryQueryque
import org.apache.commons.lang.StringUtils

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by songpeng on 16/8/31.
  */
class FetchQueryServiceImpl(facade: V62Facade, config:HallV62Config, tPCardService: TPCardService, implicit val dataSource: DataSource) extends FetchQueryService {
  /**
    * 获取比对任务, 使用ora_uuid字段及不在7.0抓取6.2记录表中，确保不被重复抓取
    *
    * @author yuchen
    * @param size
    * @param yearThreshold
    * @param dbId
    * @return
    */
  override def fetchMatchTask(size: Int, yearThreshold:String,dbId: Option[String]): Seq[MatchTask] = {

    val matchTaskList = new ArrayBuffer[MatchTask]

    val sql = new StringBuilder;
    sql ++= s"SELECT * FROM (SELECT " +
                                s"t.username " +
                                s",t.computerip" +
                                s",t.userunitcode" +
                                s",t.ora_uuid" +
                                s",t.ora_sid" +
                                s",ora_createtime" +
                                s",t.keyid" +
                                s",t.minscore" +
                                s",t.querytype" +
                                s",t.priority" +
                                s",t.maxcandnum" +
                                s",t.mic" +
                                s",t.qrycondition" +
                                s",t.textsql" +
                                s",t.flag " +
                                s"FROM NORMALQUERY_QUERYQUE  t " +
                                s"WHERE  NOT EXISTS (SELECT 1 " +
                                                    s"FROM HALL_READ_RECORD p " +
                                                    s"WHERE p.orasid=t.ora_sid) AND t.rmtflag=0 "
    if(StringUtils.isNotEmpty(yearThreshold) && StringUtils.isNotBlank(yearThreshold)){
      sql ++= s"  AND t.ora_createtime>= to_date('"+ yearThreshold +"','yyyy-mm-dd hh24:mi:ss')"
    }
    sql ++= s" ORDER BY t.ora_createtime ASC ) WHERE  ROWNUM <=?"
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
      gaQuery.createtime = rs.getTimestamp("ora_createtime")
      gaQuery.username = rs.getString("username")
      gaQuery.computerip = rs.getString("computerip")
      gaQuery.userunitcode = rs.getString("userunitcode")
      matchTaskList += ProtobufConverter.convertGafisNormalqueryQueryque2MatchTask(gaQuery)
    }

    matchTaskList.toSeq
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
    throw new UnsupportedOperationException
  }

    /**
    * 保存候选信息
      *
      * @param matchResult
    */
  override def saveMatchResult(matchResult: MatchResult, fetchConfig: HallFetchConfig, candDBIDMap: Map[String, Short] = Map()) = {
    val oraSid = getOraSidByQueryId(matchResult.getMatchId)       //oraSid
    val candNum = matchResult.getCandidateNum //candidateNum
    val maxScore = matchResult.getMaxScore   //hitpossibility
    val maxcandnum = matchResult.getMaxcandnum //maxcandnum
    val queryQue = getQueryQue(oraSid.toInt)
    val candHead = getCandHead(matchResult, queryQue)
    var candList:Array[Byte] = null
    var isChecked:Boolean = false
    var isHaveCandidate:Boolean = false
    if(candNum > 0){
      val resultMap = convertMatchResult2CandList(matchResult, queryQue.queryType, candDBIDMap, getAfisinitConfig)
      candList = resultMap("candList").asInstanceOf[Array[Byte]]
      isChecked = resultMap("isChecked").asInstanceOf[Boolean]
      isHaveCandidate = resultMap("isHaveCandidate").asInstanceOf[Boolean]
    }

    if(queryQue.queryType == QueryConstants.QUERY_TYPE_TT){
      var userunitcode = "" //单位代码
      val sql = "select userunitcode, querytype from normalquery_queryque where ora_sid = ? and rownum = 1"
      JdbcDatabase.queryWithPsSetter(sql){ps=>
        ps.setLong(1,oraSid.toLong)
      }{rs=>
        userunitcode = rs.getString("userunitcode")
      }
      //需要自动认证, 有比中候选
      if(isChecked){
        val sql : String = "update normalquery_queryque t " +
          "set t.ora_updatetime = sysdate, " +
          "t.ora_updator        = '@match+server@'," +
          "t.finishtime         = " + DateConverter.convertString2Date(matchResult.getMatchFinishTime,"yyyy-MM-dd HH:mm:ss") +  ", " +
          "t.curcandnum         = ?, " +   //matchresult.getcandidatenum
          "t.checkusername      = '$autocheck$', " +
          "t.verifyresult       = 2," +
          "t.hitpossibility     = ?, " +   //matchresult.setmaxscore
          "t.status             = '7', " +
          "t.checkerunitcode    = ?, " +
          "t.candhead           = ?, " +
          "t.candlist           = ?, " +
          "t.maxcandnum         = ?, " +
          "t.flagc              = '8', " +
          "t.flagg              = '1' " +
          "where t.ora_sid          = ? "

        JdbcDatabase.update(sql) { ps =>
          ps.setInt(1, candNum)
          ps.setInt(2, maxScore)
          ps.setString(3, userunitcode)
          ps.setBytes(4, candHead)
          ps.setBytes(5, candList)
          ps.setLong(6, maxcandnum)
          ps.setLong(7,oraSid.toLong)
        }
        //自动手动无候选未比中
      } else if(!isHaveCandidate){
        val sql : String = "update normalquery_queryque t " +
          "set t.ora_updatetime = sysdate, " +
          "t.ora_updator        = '@match+server@', " +
          "t.finishtime         = " + DateConverter.convertString2Date(matchResult.getMatchFinishTime,"yyyy-MM-dd HH:mm:ss") + ", " +
          "t.checkusername      = '$autocheck$', " +
          "t.verifyresult       = '1'," +
          "t.status             = '7'," +
          "t.checkerunitcode    = ?, " +
          "t.candhead           = ?, " +
          "t.candlist           = ?, " +
          "t.curcandnum         = ?, " +
          "t.hitpossibility     = ?, " +
          "t.maxcandnum         = ?, " +
          "t.flagg              = '96' " +
          "where t.ora_sid          = ? "

        JdbcDatabase.update(sql) { ps =>
          ps.setString(1,userunitcode)
          ps.setBytes(2, candHead)
          ps.setBytes(3, candList)
          ps.setInt(4, candNum)
          ps.setInt(5, maxScore)
          ps.setLong(6, maxcandnum)
          ps.setLong(7,oraSid.toLong)
        }
      } else {
        val sql = "update NORMALQUERY_QUERYQUE t set t.status=2, t.curcandnum=?, t.candhead=?, t.candlist=?, t.hitpossibility=?, t.FINISHTIME=" +  DateConverter.convertString2Date(matchResult.getMatchFinishTime,"yyyy-MM-dd HH:mm:ss") +" where t.ora_sid =?"
        JdbcDatabase.update(sql) { ps =>
          ps.setInt(1, candNum)
          ps.setBytes(2, candHead)
          ps.setBytes(3, candList)
          ps.setInt(4, maxScore)
          ps.setLong(5, oraSid.toLong)
        }
      }
    } else {
      val sql = "update NORMALQUERY_QUERYQUE t set t.status=2, t.curcandnum=?, t.candhead=?, t.candlist=?, t.hitpossibility=?, t.FINISHTIME=" + DateConverter.convertString2Date(matchResult.getMatchFinishTime,"yyyy-MM-dd HH:mm:ss") + " where t.ora_sid =?"
      JdbcDatabase.update(sql) { ps =>
        ps.setInt(1, candNum)
        ps.setBytes(2, candHead)
        ps.setBytes(3, candList)
        ps.setInt(4, maxScore)
        ps.setLong(5, oraSid.toLong)
      }
    }
  }

  //TODO dbid要遍历数据库查找
  private def convertMatchResult2CandList(matchResult: MatchResult, queryType: Int, candDBIDMap: Map[String, Short] = Map(), configMap : scala.collection.mutable.HashMap[String, String]): scala.collection.mutable.HashMap[String, Any] ={
    val resultMap = new scala.collection.mutable.HashMap[String,Any]
    var isChecked = false
    val result = new ByteArrayOutputStream()
    val candIter = matchResult.getCandidateResultList.iterator()
    var isHaveCandidate = false
    if(matchResult.getCandidateResultList.size() > 0){
      isHaveCandidate = true
    }
    var index = 0 //比对排名
    while (candIter.hasNext) {
      index += 1
      val cand = candIter.next()
      val gCand = new GAQUERYCANDSTRUCT
      gCand.nScore = cand.getScore
      gCand.szKey = cand.getObjectId
      //根据候选卡号获取dbid，如果没有使用默认库dbid
      if(candDBIDMap.nonEmpty && candDBIDMap.get(cand.getObjectId).nonEmpty){
        gCand.nDBID = candDBIDMap.get(cand.getObjectId).get
      }else{
        gCand.nDBID = if (queryType == QueryConstants.QUERY_TYPE_TT || queryType == QueryConstants.QUERY_TYPE_TL) 1 else 2
      }
      gCand.nTableID = 2
      gCand.nIndex = cand.getPos.toByte
      gCand.tFinishTime = DateConverter.convertString2AFISDateTime(cand.getMatchFinishTime)
      gCand.nStepOneRank = index
      val gCandArray: Array[Byte] = gCand.toByteArray()
      val TTSearchQryAutoCheckLocal = configMap("TTSearchQryAutoCheckLocal")      //自动认定
      val TTAutoMergeCandScore = configMap("TTAutoMergeCandScore")                //大于此值自动比中
      if(null != TTSearchQryAutoCheckLocal
                && TTSearchQryAutoCheckLocal == "1"
                && queryType == QueryConstants.QUERY_TYPE_TT){
        if(null != TTAutoMergeCandScore && gCand.nScore >= TTAutoMergeCandScore.toInt){
          //复核状态
          gCandArray(48) = 1
          gCandArray(57) = 6
          isChecked = true
        }
      }
      result.write(gCandArray)
    }
    resultMap("candList") = result.toByteArray
    resultMap("isChecked") = isChecked
    resultMap("isHaveCandidate") = isHaveCandidate
    return resultMap
  }



  /**
    * 获取查询信息
    *
    * @param orasid
    * @return
    */
  override def getQueryQue(orasid: Int): QueryQue = {
    val sql = "select t.keyid, t.querytype, t.flag from NORMALQUERY_QUERYQUE t where t.ora_sid =?"
    JdbcDatabase.queryFirst(sql) { ps =>
      ps.setInt(1, orasid)
    } { rs =>
      val keyId = rs.getString("keyid")
      val queryType = rs.getInt("querytype")
      val flag = rs.getInt("flag")
      new QueryQue(keyId, orasid, queryType, if (flag == 2 || flag == 22) true else false)
    }.get
  }

   /**
    * 获得配置信息
    */
  private def getAfisinitConfig() : scala.collection.mutable.HashMap[String, String] = {
    val configMap = new scala.collection.mutable.HashMap[String, String]
    val sql = "select * from ADMIN_AFISINIT where key in (?,?) "
    JdbcDatabase.queryWithPsSetter(sql){ps=>
      ps.setString(1,"TTSearchQryAutoCheckLocal")
      ps.setString(2,"TTAutoMergeCandScore")
    }{rs=>
      val key = rs.getString("key")
      val value = rs.getString("value")
      configMap(key) = value
    }
    return configMap
  }

  private def getOraSidByQueryId(queryId:String):String ={
    val sql = s"SELECT orasid from GAFIS_TASK70RECORD where queryid=?"
    var oraSid = ""
    JdbcDatabase.queryWithPsSetter(sql){ps=>
      ps.setString(1,queryId)
    }{rs=>
      oraSid = rs.getString("orasid")
    }
    oraSid
  }

  /**
    * 保存6.2已经给7.0抓取的任务的任务号
    *
    * @author yuchen
    * @param oraSid
    * @return
    */
  override def saveFetchRecord(oraSid:String)={
    val sql = s"INSERT INTO HALL_READ_RECORD (UUID,ORASID,Createtime) " +
              s"VALUES (?,?,sysdate)"
    JdbcDatabase.update(sql){ ps =>
      ps.setString(1,java.util.UUID.randomUUID().toString.replace("-",""))
      ps.setString(2,oraSid)
    }
  }

  /**
    * 获得没有同步候选的比对任务的任务号
    *
    * @author yuchen
    * @param size 单次请求数量
    */
  override def getTaskNumWithNotSyncCandList(size: Int): ListBuffer[mutable.HashMap[String,Any]] = {
    val sql = s"SELECT * FROM  (SELECT " +
                                  s"t.uuid" +
                                  s",t.queryid" +
                                  s",t.orasid" +
                                  s",t.querytype" +
                                  s",t.keyid " +
                              s" FROM GAFIS_TASK70RECORD t " +
                              s" WHERE t.issynccandlist = '0'  " +
                              s" ORDER BY t.createtime ASC )   " +
             s"WHERE  ROWNUM <=?"
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

  override def updateStatusWithGafis_Task62Record(status: String,uuid:String): Unit = {
    val sql = s"UPDATE GAFIS_TASK70RECORD t SET t.issynccandlist=?,t.updatetime=sysdate WHERE t.uuid=?"
    JdbcDatabase.update(sql){ ps =>
      ps.setString(1,status)
      ps.setString(2,uuid)
    }
  }


  override def recordGafisTask(objectId: String, queryId: String, isSyncCandList: String, matchType: String, cardId: String, pkId: String): Unit = {

    val sql = s"INSERT INTO GAFIS_TASK70RECORD (UUID" +
                                            s",queryid" +
                                            s",orasid" +
                                            s",issynccandlist" +
                                            s",createtime" +
                                            s",updatetime" +
                                            s",querytype" +
                                            s",keyid" +
                                            s",pkid) " +
              s"VALUES (?,?,?,?,sysdate,?,?,?,?)"
    JdbcDatabase.update(sql){ ps =>
      ps.setString(1,java.util.UUID.randomUUID().toString.replace("-",""))
      ps.setString(2,objectId)
      ps.setString(3,queryId)
      ps.setInt(4,isSyncCandList.toInt)
      ps.setString(5,"")
      ps.setInt(6,matchType.toInt)
      ps.setString(7,cardId)
      ps.setString(8,pkId)
    }
  }
}

