package nirvana.hall.api.services.sync

import nirvana.hall.protocol.api.FPTProto.TPCard

/**
 * Created by songpeng on 16/8/17.
 */
trait FetchTPCardService {

  /**
   * 获取捺印同步卡号列表
   * @param seq
   * @param size
   * @param dbId
   */
  def fetchCardId(seq: Long, size: Int, dbId: Option[String] = None): Seq[(String, Long)]

  /**
   * 验证读取策略
   * @param tpCard
   * @param readStrategy
   * @return
   */
  def validateByReadStrategy(tpCard: TPCard, readStrategy: String): Boolean
}
