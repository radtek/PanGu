package nirvana.hall.v62.internal


import java.sql.Timestamp
import java.util.UUID
import javax.sql.DataSource

import nirvana.hall.api.services.AssistCheckRecordService
import nirvana.hall.support.services.JdbcDatabase

import scala.collection.mutable.{HashMap, ListBuffer}

/**
  * Created by yuchen on 2017/4/21.
  */
class AssistCheckRecordServiceImpl(implicit val dataSource: DataSource) extends AssistCheckRecordService {
  override def recordAssistCheck(queryId: String, oraSid: String, caseId: String, personId: String, source: String): Unit = {

    val sql = "insert into HALL_ASSISTCHECK(id,queryid,orasid,caseid,personid,createtime,source，status) VALUES(?,?,?,?,?,sysdate,?,?)"
    JdbcDatabase.update(sql) { ps =>
      ps.setString(1,UUID.randomUUID().toString.replace("-",""))
      ps.setString(2,queryId)
      ps.setString(3,oraSid)
      ps.setString(4,caseId)
      ps.setString(5,personId)
      ps.setString(6,source)
      ps.setLong(7,0)
    }
  }

  /**
    * 查询协查任务
    */
  override def findAssistcheck(size: String): ListBuffer[HashMap[String, Any]] = {
    var resultList = new ListBuffer[HashMap[String, Any]]
    val sql = "select a.id, a.queryid, a.orasid, a.caseid, a.personid, q.querytype, q.keyid " +
      "from hall_assistcheck a, normalquery_queryque q " +
      "where a.queryid = q.queryid and a.orasid = q.ora_sid and a.status = '7' and ((q.querytype = 0 and q.status = 7) or (q.querytype = 2 and q.status = 11)) " +
      "and rownum <= ?"
    JdbcDatabase.queryWithPsSetter(sql) { ps =>
      ps.setString(1, size)
    } { rs =>
      var resultMap = new HashMap[String, Any]
      resultMap += ("id" -> rs.getString("id"))
      resultMap += ("queryid" -> rs.getString("queryid"))
      resultMap += ("orasid" -> rs.getString("orasid"))
      resultMap += ("caseid" -> rs.getString("caseid"))
      resultMap += ("personid" -> rs.getString("personid"))
      resultMap += ("querytype" -> rs.getLong("querytype"))
      resultMap += ("keyid" -> rs.getString("keyid"))
      resultList += resultMap
    }
    resultList
  }

  /**
    * 更新协查任务状态
    *
    * @param status
    * @param id
    */
  override def updateAssistcheckStatus(status: Long, id: String): Unit = {
    val sql = "update hall_assistcheck set status = ?, updatetime = sysdate where id = ? "
    JdbcDatabase.update(sql) { ps =>
      ps.setLong(1, status)
      ps.setString(2, id)
    }
  }

  /**
    * 更新协查任务
    *
    * @param status
    * @param id
    * @param fptPath
    */
  override def updateAssistcheck(status: Long, id: String, fptPath: String): Unit = {
    val sql = "update hall_assistcheck set status = ?, updatetime = sysdate, fpt_path = ?  where id = ? "
    JdbcDatabase.update(sql) { ps =>
      ps.setLong(1, status)
      ps.setString(2, fptPath)
      ps.setString(3, id)
    }
  }

  /**
    * 查询未上报正查查重比对关系
    */
  override def findUploadCheckin(uploadTime: String, size: String): ListBuffer[HashMap[String, Any]] = {
    var resultList = new ListBuffer[HashMap[String, Any]]
    val sql = "select q.queryid, q.ora_uuid, q.ora_sid, q.querytype,q.keyid from normalquery_queryque q " +
      "where not exists (select serviceid from hall_xc_report where serviceid = q.ora_uuid) " +
      "and ((q.querytype = 0 and q.status = 7) or (q.querytype = 2 and q.status = 11) or (q.querytype = 1 and q.status = 11) or (q.querytype = 3 and q.status = 7)) and queryid = 0 " +
      "and to_char(q.ora_createtime,'yyyy') = ? and rownum <= ?"
    JdbcDatabase.queryWithPsSetter(sql) { ps =>
      ps.setString(1, uploadTime)
      ps.setString(2, size)
    } { rs =>
      var resultMap = new HashMap[String, Any]
      resultMap += ("queryid" -> rs.getString("queryid"))
      resultMap += ("oraUuid" -> rs.getString("ora_uuid"))
      resultMap += ("oraSid" -> rs.getString("ora_sid"))
      resultMap += ("querytype" -> rs.getLong("querytype"))
      resultMap += ("keyid" -> rs.getString("keyid"))
      resultList += resultMap
    }
    resultList
  }

  /**
    * 保存上报记录
    */
  override def saveXcReport(serviceid: String, typ: String, status: Long, fptPath: String): Unit = {
    val uuid = UUID.randomUUID().toString.replace("-", "")
    val sql = "insert into hall_xc_report(id,serviceid,typ,status,fpt_path,create_time,update_time) values(?,?,?,?,?,sysdate,sysdate) "
    JdbcDatabase.update(sql) { ps =>
      ps.setString(1, uuid)
      ps.setString(2, serviceid)
      ps.setString(3, typ)
      ps.setLong(4, status)
      ps.setString(5, fptPath)
    }

  }

  override def saveErrorReport(serviceid: String, typ: String, status: Long, msg: String): Unit = {
    val uuid = UUID.randomUUID().toString.replace("-", "")
    val sql = "insert into hall_xc_report(id,serviceid,typ,status,errormsg,create_time,update_time) values(?,?,?,?,?,sysdate,sysdate) "
    JdbcDatabase.update(sql) { ps =>
      ps.setString(1, uuid)
      ps.setString(2, serviceid)
      ps.setString(3, typ)
      ps.setLong(4, status)
      ps.setString(5, msg)
    }
  }


  override def updateAssistcheckLT(queryId: String, oraSid: String, caseId: String, id: String, status: Int, msg: String): Unit = {
    val sql = s"UPDATE HALL_ASSISTCHECK " +
      s"SET QUERYID = ?,ORASID = ?,CASEID = ?,STATUS = ?,ORA_UUID = ?,UPDATETIME = sysdate,SERVICE_TYPE = ?,FINGERID = ?,ERRORMSG = ? " +
      s"WHERE id = ? "
    JdbcDatabase.update(sql) { ps =>
      ps.setString(1, queryId)
      ps.setString(2, oraSid)
      ps.setString(3, caseId)
      ps.setInt(4, status)
      ps.setString(5, UUID.randomUUID.toString.replace("-", ""))
      ps.setString(6, "7")
      ps.setString(7, caseId)
      ps.setString(8, msg)
      ps.setString(9, id)

    }
  }


  override def updateAssistcheckTT(queryId: String, oraSid: String, personId: String, id: String, status: Int, msg: String): Unit = {
    val sql = s"UPDATE HALL_ASSISTCHECK " +
      s"SET QUERYID = ?,ORASID = ?,PERSONID = ?,STATUS = ?,ORA_UUID = ?,UPDATETIME = sysdate,SERVICE_TYPE = ?,ERRORMSG = ? " +
      s"WHERE id = ? "
    JdbcDatabase.update(sql) { ps =>
      ps.setString(1, queryId)
      ps.setString(2, oraSid)
      ps.setString(3, personId)
      ps.setInt(4, status)
      ps.setString(5, UUID.randomUUID.toString.replace("-", ""))
      ps.setString(6, "8")
      ps.setString(7, msg)
      ps.setString(8, id)

    }
  }

  override def saveXcQuery(uuid: String,taskid:String, fingerid: String, typ: Int, status: Int, custom1: String, custom2: String, detail: String, errorinfo: String, date: Timestamp): Unit = {
    val sql = "insert into xc_query(id,task_id,keyid,service_type,status,custom1,custom2,errorinfo,create_time,detail) values(?,?,?,?,?,?,?,?,?,?) "
    JdbcDatabase.update(sql) { ps =>
      ps.setString(1, uuid)
      ps.setString(2, taskid)
      ps.setString(3, fingerid)
      ps.setInt(4, typ)
      ps.setInt(5, status)
      ps.setString(6, custom1)
      ps.setString(7, custom2)
      ps.setString(8, errorinfo)
      ps.setTimestamp(9, date)
      ps.setString(10, detail)
    }
  }

  override def updateXcQuery(uuid: String, serviceid:String, fingerid: String, typ: Int, status: Int, oraSid: String, custom2: String, errorinfo: String, date: Timestamp): Unit = {
    val sql = s"UPDATE xc_query " +
      s"SET status = ?,serviceid = ?,custom1 = ?, errorinfo = ?,custom2 = ? , update_time = sysdate " +
      s"WHERE id = ? and keyid = ? and service_type = ? and create_time = ?"
    JdbcDatabase.update(sql) { ps =>
      ps.setInt(1, status)
      ps.setString(2, serviceid)
      ps.setString(3, oraSid)
      ps.setString(4, errorinfo)
      ps.setString(5, custom2)
      ps.setString(6, uuid)
      ps.setString(7, fingerid)
      ps.setInt(8, typ)
      ps.setTimestamp(9, date)
    }
  }


  override def updateXcTask(id: String, status: Int, errorinfo: String, detail: String, serviceid: String, custom2: String): Unit = {
    val sql = s"UPDATE xc_task " +
      s"SET status = ? , custom2 = ?,serviceid= ?, detail = ?,errorinfo = ?" +
      s"WHERE id = ?"
    JdbcDatabase.update(sql) { ps =>
      ps.setInt(1, status)
      ps.setString(2, custom2)
      ps.setString(3, serviceid)
      ps.setString(4, detail)
      ps.setString(5, errorinfo)
      ps.setString(6, id)
    }
  }


  override def updateXcTask(id: String, status: Int, errorinfo: String, detail: String, serviceid: String, custom2: String, custom4: String): Unit = {
    val sql = s"UPDATE xc_task " +
      s"SET status = ? , custom2 = ?,custom4 = ?, serviceid = ?,detail = ?,errorinfo= ? " +
      s"WHERE id = ?"
    JdbcDatabase.update(sql) { ps =>
      ps.setInt(1, status)
      ps.setString(2, custom2)
      ps.setString(3, custom4)
      ps.setString(4, serviceid)
      ps.setString(5, detail)
      ps.setString(6, errorinfo)
      ps.setString(7, id)
    }
  }

  override def updateXcTask(id: String, executetimes: Int): Unit = {
    val sql = s"UPDATE xc_task " +
      s"SET executetimes = ? " +
      s"WHERE id = ?"
    JdbcDatabase.update(sql) { ps =>
      ps.setInt(1, executetimes)
      ps.setString(2, id)
    }
  }

  override def getOraUuid(queryId: Long): String = {
    var ora_uuid = ""
    val sql = s"select t.ora_uuid from NORMALQUERY_QUERYQUE t where t.ora_sid = ?"
    JdbcDatabase.queryFirst(sql) { ps =>
      ps.setLong(1, queryId)
    } { rs =>
      ora_uuid = rs.getString("ora_uuid")
    }
    ora_uuid
  }
}
