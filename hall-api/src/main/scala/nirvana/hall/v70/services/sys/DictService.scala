package nirvana.hall.v70.services.sys

import nirvana.hall.protocol.sys.DictProto.{DictData, DictType}

/**
 * 字典同步 service
 * Created by songpeng on 15/11/4.
 */
trait DictService {

  /**
   * 根据字典类型获取全部字典数据
   * @param dictType 字典类型
   * @return
   */
  def findAllDict(dictType: DictType): Seq[DictData]

  /**
   * 查询字典列表
   * @param dictType 字典类型
   * @return
   */
  def findDictList(dictType: DictType, code: Option[String], name: Option[String], from:Int = 0, size:Int ) :Seq[(String, String)]
}
