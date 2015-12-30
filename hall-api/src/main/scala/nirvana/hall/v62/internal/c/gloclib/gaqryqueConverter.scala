package nirvana.hall.v62.internal.c.gloclib

import java.nio.ByteBuffer

import nirvana.hall.c.services.gbaselib.gitempkg.{GBASE_ITEMPKG_ITEMHEADSTRUCT, GBASE_ITEMPKG_PKGHEADSTRUCT}
import nirvana.hall.c.services.gloclib.gadbprop.GADBIDSTRUCT
import nirvana.hall.c.services.gloclib.gaqryque
import nirvana.hall.c.services.gloclib.gaqryque.GAQUERYSTRUCT
import nirvana.hall.c.services.gloclib.gqrycond.GAFIS_QRYPARAM
import nirvana.hall.protocol.matcher.MatchResult.MatchResultRequest
import nirvana.hall.protocol.matcher.MatchTaskQueryProto.MatchTask
import nirvana.hall.protocol.matcher.NirvanaTypeDefinition.MatchType
import nirvana.hall.v62.config.HallV62Config
import org.jboss.netty.buffer.ChannelBuffers

/**
 * Created by songpeng on 15/12/9.
 */
object gaqryqueConverter {
  final val GAFIS_KEYLIST_GetName = "KeyList"
  final val GAFIS_QRYPARAM_GetName = "QryParam"
  final val GAFIS_QRYFILTER_GetName = "QryFilter"
  final val GAFIS_CANDKEYFILTER_GetName = "CandKeyFilter"
  final val GAFIS_TEXTSQL_GetName = "TextSql"

  def convertProtoBuf2GAQUERYSTRUCT(matchTask: MatchTask)(implicit v62Config: HallV62Config): GAQUERYSTRUCT = {
    val queryStruct = new GAQUERYSTRUCT
    queryStruct.stSimpQry.nQueryType = matchTask.getMatchType.ordinal().asInstanceOf[Byte]
    queryStruct.stSimpQry.nPriority = matchTask.getPriority.toByte
    queryStruct.stSimpQry.nFlag = (gaqryque.GAQRY_FLAG_USEFINGER).asInstanceOf[Byte]

    matchTask.getMatchType match {
      case MatchType.FINGER_TT =>
        queryStruct.stSimpQry.stSrcDB.nDBID = v62Config.templateTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stSrcDB.nTableID= v62Config.templateTable.tableId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB = Array(new GADBIDSTRUCT)
        queryStruct.stSimpQry.nDestDBCount = 1
        queryStruct.stSimpQry.stDestDB.apply(0).nDBID = v62Config.templateTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB.apply(0).nTableID= v62Config.templateTable.tableId.asInstanceOf[Short]
      case MatchType.FINGER_TL =>
        queryStruct.stSimpQry.stSrcDB.nDBID = v62Config.templateTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stSrcDB.nTableID= v62Config.templateTable.tableId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB = Array(new GADBIDSTRUCT)
        queryStruct.stSimpQry.nDestDBCount = 1
        queryStruct.stSimpQry.stDestDB.apply(0).nDBID = v62Config.latentTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB.apply(0).nTableID= v62Config.latentTable.tableId.asInstanceOf[Short]
      case MatchType.FINGER_LT =>
        queryStruct.stSimpQry.stSrcDB.nDBID = v62Config.latentTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stSrcDB.nTableID= v62Config.latentTable.tableId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB = Array(new GADBIDSTRUCT)
        queryStruct.stSimpQry.nDestDBCount = 1
        queryStruct.stSimpQry.stDestDB.apply(0).nDBID = v62Config.templateTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB.apply(0).nTableID= v62Config.templateTable.tableId.asInstanceOf[Short]
      case MatchType.FINGER_LL =>
        queryStruct.stSimpQry.stSrcDB.nDBID = v62Config.latentTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stSrcDB.nTableID= v62Config.latentTable.tableId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB = Array(new GADBIDSTRUCT)
        queryStruct.stSimpQry.nDestDBCount = 1
        queryStruct.stSimpQry.stDestDB.apply(0).nDBID = v62Config.latentTable.dbId.asInstanceOf[Short]
        queryStruct.stSimpQry.stDestDB.apply(0).nTableID= v62Config.latentTable.tableId.asInstanceOf[Short]
      case other =>
    }

    //设置比对参数
    val item = new GAFIS_QRYPARAM
    item.stXgw.bFullMatchOn = matchTask.getConfig.getFullMatchOn.asInstanceOf[Byte]

    val itemDataLength = item.getDataSize
    val itemHead = new GBASE_ITEMPKG_ITEMHEADSTRUCT
    itemHead.szItemName = GAFIS_TEXTSQL_GetName
    itemHead.nItemLen = itemDataLength

    val itemPackage = new GBASE_ITEMPKG_PKGHEADSTRUCT
    itemPackage.nDataLen = itemPackage.getDataSize + itemHead.getDataSize + itemHead.nItemLen
    itemPackage.nBufSize = itemPackage.nDataLen

    val buffer = ChannelBuffers.buffer(itemPackage.nDataLen)
    itemPackage.writeToStreamWriter(buffer)

    itemHead.writeToStreamWriter(buffer)
    item.writeToStreamWriter(buffer)

    val bytes = buffer.array()
    queryStruct.pstQryCond_Data = bytes
    queryStruct.nQryCondLen = queryStruct.pstQryCond_Data.length

    queryStruct.nItemFlagA = gaqryque.GAIFA_FLAG.asInstanceOf[Byte]

    queryStruct
  }

  def convertQueryId2GAQUERYSTRUCT(queryId: Long):GAQUERYSTRUCT ={
    val pstQry = new GAQUERYSTRUCT
    pstQry.stSimpQry.nQueryID = convertLongAsSixByteArray(queryId)
    pstQry
  }

  def convertGAQUERYSTRUCT2ProtoBuf(gaQueryStruct: GAQUERYSTRUCT): MatchResultRequest ={
    val matchResultRequest = MatchResultRequest.newBuilder()

    val queryId = gaQueryStruct.stSimpQry.nQueryID
    matchResultRequest.setMatchId(convertSixByteArrayToLong(queryId)+"")

    val pstCandData = gaQueryStruct.pstCand_Data
    matchResultRequest.setCandidateNum(pstCandData.length)

    var maxScore = 0

    pstCandData.foreach{ candData =>
      val matchResult = matchResultRequest.addCandidateResultBuilder()
      matchResult.setObjectId(convertSixByteArrayToLong(candData.nSID))
      matchResult.setPos(candData.nIndex)
      matchResult.setScore(candData.nScore)
      if(maxScore < candData.nScore)
        maxScore = candData.nScore
    }
    matchResultRequest.setMaxScore(maxScore)

    matchResultRequest.build()
  }

  /**
   * 转换6个字节成long
   * @param bytes 待转换的六个字节
   * @return 转换后的long数字
   */
  def convertSixByteArrayToLong(bytes: Array[Byte]): Long = {
    /*
    val byteBuffer = ByteBuffer.allocate(8).put(Array[Byte](0,0)).put(bytes)
    byteBuffer.rewind()
    byteBuffer.getLong
    */
    var l = 0L
    l |= (0xff & bytes(0)) << 16
    l |= (0xff & bytes(1)) << 8
    l |= (0xff & bytes(2))
    l <<= 24

    l |= (0xff & bytes(3)) << 16
    l |= (0xff & bytes(4)) << 8
    l |= (0xff & bytes(5))

    l
  }
  def convertLongAsSixByteArray(sid: Long): Array[Byte]=
    ByteBuffer.allocate(8).putLong(sid).array().slice(2,8)
}
