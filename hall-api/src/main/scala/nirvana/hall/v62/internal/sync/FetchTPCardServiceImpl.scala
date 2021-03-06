package nirvana.hall.v62.internal.sync

import javax.sql.DataSource

import nirvana.hall.api.services.sync.FetchTPCardService
import nirvana.hall.protocol.api.FPTProto.TPCard
import nirvana.hall.v62.internal.V62Facade
import org.apache.tapestry5.json.JSONObject

import scala.collection.mutable.ArrayBuffer

/**
  * Created by songpeng on 16/8/17.
  */
class FetchTPCardServiceImpl(implicit dataSource: DataSource) extends SyncDataFetcher with FetchTPCardService{
  override val KEY_NAME: String = "cardid"
  override def fetchCardId(seq: Long, size: Int, dbId: Option[String]): Seq[(String, Long)]= {
    val cardIdList = new ArrayBuffer[(String, Long)]
    val tableName = getTableName(dbId.get.toShort, V62Facade.TID_TPCARDINFO)
    doFetcher(cardIdList, seq, size, tableName)
    cardIdList.toSeq
  }

  /**
    * 验证读取策略
    *
    * @param tpCard
    * @param readStrategy
    * @return
    */
  override def validateByReadStrategy(tpCard: TPCard, readStrategy: String): Boolean = {
    true
  }
}
