package nirvana.hall.v70.internal.sync

import javax.sql.DataSource

import nirvana.hall.api.services.sync.FetchLPCardService
import nirvana.hall.protocol.api.FPTProto.LPCard
import org.apache.tapestry5.json.JSONObject

import scala.collection.mutable.ArrayBuffer

/**
  * Created by songpeng on 16/8/22.
  */
class FetchLPCardServiceImpl (implicit dataSource: DataSource) extends SyncDataFetcher with FetchLPCardService{
  override val SYNC_SQL: String = "select t.finger_id as sid, t.seq from gafis_case_finger t left join gafis_logic_db_case db on t.case_id=db.case_pkid where db.logic_db_pkid =? and t.seq >=? and t.seq <=?"
  //override val SYNC_SQL: String = "select t.finger_id as sid, t.seq from gafis_case_finger t left join gafis_logic_db_case db on t.case_id=db.case_pkid where db.logic_db_pkid =? and t.seq >=? and t.seq <= ? and t.finger_id not in (select h.serviceid from hall_ds_person h where h.status != '0')"

  /**
    * 获取最大的seq值
    *
    * @return
    */
  override def getMaxSeq(dbId: Option[String])(implicit dataSource: DataSource): Long = {
    getSeqBySql(s"select max(t.seq) from gafis_case_finger t left join gafis_logic_db_case db on t.case_id=db.case_pkid where db.logic_db_pkid='${dbId.get}'")
  }

  /**
    * 获取最小的seq值, 大于from
    *
    * @param from
    * @return
    */
  override def getMinSeq(from: Long, dbId: Option[String])(implicit dataSource: DataSource): Long = {
    getSeqBySql(s"select min(t.seq) from gafis_case_finger t left join gafis_logic_db_case db on t.case_id=db.case_pkid where db.logic_db_pkid='${dbId.get}' and t.seq > ${from} ")
    //getSeqBySql(s"select min(t.seq) from gafis_case_finger t left join gafis_logic_db_case db on t.case_id=db.case_pkid where db.logic_db_pkid='${dbId.get}' and t.seq > ${from} and t.finger_id not in (select t1.serviceid from hall_ds_case_finger t1 where t1.status != '0')")
  }

  /**
    * 获取案件列表
    *
    * @param seq
    * @param size
    * @param dbId
    */
  override def fetchCardId(seq: Long, size: Int, dbId: Option[String]): Seq[(String, Long)] = {
    val cardIdList = new ArrayBuffer[(String, Long)]()
    doFetcher(cardIdList, seq, size, dbId)
    cardIdList
  }

  /**
    * 验证读取策略
    *
    * @param lPCard
    * @param readStrategy
    * @return
    */
  override def validateByReadStrategy(lPCard: LPCard, readStrategy: String): Boolean = {

    val strategy = new JSONObject(readStrategy)
    if(strategy.has("datasource")){
      val dataSourceStrategy = strategy.getString("datasource")
      val dataSource = lPCard.getStrDataSource
      if(dataSourceStrategy.startsWith("!")){
        if(dataSource.equals(dataSourceStrategy.substring(1,dataSourceStrategy.length))){
          return false
        }
      }else{
        if(!dataSource.equals(dataSourceStrategy)){
          return false
        }
      }
    }

    if(strategy.has("cardid")){
      val cardIdStrategy = strategy.getString("cardid")
      val cardId = lPCard.getStrCardID
      if(cardIdStrategy.startsWith("=")){
        if(!cardId.startsWith(cardIdStrategy.substring(1,cardIdStrategy.length))){
          return false
        }
      }else{
        if(!cardId.startsWith(cardIdStrategy)){
          return false
        }
      }
    }
    true
  }
}
