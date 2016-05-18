package nirvana.hall.v62.internal.c

import nirvana.hall.c.services.AncientData
import nirvana.hall.c.services.ganumia.gadbrec
import nirvana.hall.c.services.ganumia.gadbrec.{GADB_SELRESULT, GADB_SELSTATEMENT}
import nirvana.hall.v62.internal.c.gnetlib.nettable
import org.jboss.netty.buffer.ChannelBuffers

import scala.reflect._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-18
  */
trait V62QueryTableSupport {
  this:nettable=>
  /**
    * 通用查询接口
    *
    * @param dbId          数据库ID
    * @param tableId       表ID
    * @param mapper        数据库字段映射到实体对象的字段，键:数据库列名,值:实体的字段名称
    * @param statementOpt  查询的SQL语句
    * @param limit         查前多少条，默认为30
    * @tparam T             实体类型
    * @return 查询到的实体集合
    */
  def queryV62Table[T <: AncientData : ClassTag](dbId:Short,
                                                 tableId:Short,
                                                 mapper:Map[String,String],
                                                 statementOpt:Option[String],
                                                 limit:Int=30):List[T]={

    /**
      * 首先构造查询列到实体字段的映射
      */
    val stSelRes = new GADB_SELRESULT
    val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    val destStruct = clazz.newInstance()
    stSelRes.nSegSize = destStruct.getDataSize
    val stItems = mapper.map{ case (column,field)=>
      gadbrec.SETSELRESITEM_FIXED(destStruct, column,field)
    }
    stSelRes.pstItem_Data = stItems.toArray
    stSelRes.nResItemCount = stItems.size

    //构造查询条件
    val stSelStatement = new GADB_SELSTATEMENT
    stSelStatement.nMaxToGet = limit
    statementOpt.foreach(stSelStatement.szStatement = _)

    val ret = NET_GAFIS_TABLE_Select(dbId,tableId, stSelRes, stSelStatement);
    if(ret >= 0){
      val count = stSelRes.nRecGot
      if(count > 0) {
        val buffer = ChannelBuffers.wrappedBuffer(stSelRes.pDataBuf_Data)
        return Range(0, count).map(x => clazz.newInstance().fromStreamReader(buffer)).toList
      }
    }
    Nil
  }
}
