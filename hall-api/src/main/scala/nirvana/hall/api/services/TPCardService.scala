package nirvana.hall.api.services

import nirvana.hall.c.services.gfpt4lib.FPT4File.Logic02Rec
import nirvana.hall.protocol.api.FPTProto.TPCard
import org.springframework.transaction.annotation.Transactional

/**
 * Created by songpeng on 16/1/26.
 */
trait TPCardService {

  /**
   * 新增捺印卡片
   * @param tPCard
   * @return
   */
  @Transactional
  def addTPCard(tPCard: TPCard, dbId: Option[String] = None): Unit

  /**
   * 删除捺印卡片
   * @param cardId
   * @return
   */
  @Transactional
  def delTPCard(cardId: String, dbId: Option[String] = None): Unit

  /**
   * 更新捺印卡片
   * @param tpCard
   * @return
   */
  @Transactional
  def updateTPCard(tpCard: TPCard, dbId: Option[String] = None): Unit

  /**
   * 验证卡号是否已存在
   * @param cardId
   * @return
   */
  def isExist(cardId: String, dbId: Option[String] = None): Boolean

  /**
   * 获取捺印卡信息
   * @param cardId
   * @param dbid
   * @return
   */
  def getTPCard(cardId: String, dbid: Option[String] = None): TPCard

  /**
    * 查询捺印文本信息
    * @param ryno        人员编号
    * @param xm          姓名
    * @param xb          性别
    * @param idno        身份证号码
    * @param zjlb        证件类别
    * @param zjhm        证件号码
    * @param hjddm       户籍地代码
    * @param xzzdm       现住址代码
    * @param rylb        人员类别
    * @param ajlb        案件类别
    * @param qkbs        前科标识
    * @param xcjb        协查级别
    * @param nydwdm      捺印单位代码
    * @param startnydate 开始时间（检索捺印时间，时间格式YYYYMMDDHHMM）
    * @param endnydate   结束时间（检索捺印时间，时间格式YYYYMMDDHHMM）
    * @return Logic02Rec(fpt4捺印文本信息)
    */
  def getFPT4Logic02RecList(ryno: String, xm: String, xb: String, idno: String, zjlb: String, zjhm: String, hjddm: String, xzzdm: String, rylb: String, ajlb: String, qkbs: String, xcjb: String, nydwdm: String, startnydate: String, endnydate: String): Seq[Logic02Rec]

  /**
    * 针对海鑫综采对接使用
    * @param tpCard
    * @param dbId
    */
  def addTPCardHXZC(tpCard: TPCard, dbId: Option[String] = None):Unit

}
