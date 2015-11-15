package nirvana.hall.v62.internal.c.gloclib

import com.google.protobuf.ProtocolStringList
import nirvana.hall.protocol.v62.FPTProto
import nirvana.hall.protocol.v62.FPTProto.{Case, LPCard}
import nirvana.hall.v62.AncientConstants
import nirvana.hall.v62.internal.c.gbaselib.gbasedef.GAKEYSTRUCT
import nirvana.hall.v62.internal.c.gloclib.galoclp.{GCASEINFOSTRUCT, GLPCARDINFOSTRUCT}
import nirvana.hall.v62.internal.c.gloclib.glocdef.{GAFISMICSTRUCT, GATEXTITEMSTRUCT}

import scala.collection.mutable
import scala.collection.JavaConversions._

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2015-11-14
 */
object galoclpConverter {
  /**
   * convert protobuf object to latent card object
   * @param card protobuf object
   * @return gafis latent card object
   */
  def convertProtoBuf2GLPCARDINFOSTRUCT(card: LPCard): GLPCARDINFOSTRUCT= {
    val data = new GLPCARDINFOSTRUCT
    data.szCardID = card.getStrCardID

    if(card.hasText) {
      val text = card.getText

      val buffer = mutable.Buffer[GATEXTITEMSTRUCT]()

      //text information
      appendTextStruct(buffer, "SeqNo",text.getStrSeq)
      appendTextStruct(buffer, "RemainPlace",text.getStrRemainPlace)
      appendTextStruct(buffer, "RidgeColor",text.getStrRidgeColor)
      if(text.hasBDeadBody)
        appendTextStruct(buffer, "IsUnknownBody",if(text.getBDeadBody) "1" else "0")
      appendTextStruct(buffer, "UnknownBodyCode",text.getStrDeadPersonNo)
      if(text.hasNXieChaState)
        appendTextStruct(buffer, "XieChaFlag",text.getNXieChaState.toString)
      if(text.hasNBiDuiState)
        appendTextStruct(buffer, "BiDuiState",text.getNBiDuiState.toString)
      appendTextStruct(buffer, "LatStart",text.getStrStart)
      appendTextStruct(buffer, "LatEnd",text.getStrEnd)


      data.pstText_Data = buffer.toArray
      data.nTextItemCount = buffer.size.asInstanceOf[Short]
    }

    if(card.hasBlob){
      val blob = card.getBlob
      val mic = new GAFISMICSTRUCT
      var flag = 0
      if(blob.hasStMnt){
        mic.pstMnt_Data = blob.getStMntBytes.toByteArray
        mic.nMntLen = mic.pstMnt_Data.length

        flag |= glocdef.GAMIC_ITEMFLAG_MNT
      }
      if(blob.hasStImage){
        val imgType = blob.getStImageBytes.byteAt(9) //see tagGAFISIMAGEHEADSTRUCT.bIsCompressed
        if(imgType == 1){ //image compressed
          mic.pstCpr_Data = blob.getStImageBytes.toByteArray
          mic.nCprLen = mic.pstCpr_Data.length
          flag |= glocdef.GAMIC_ITEMFLAG_CPR
        }else{
          mic.pstImg_Data = blob.getStImageBytes.toByteArray
          mic.nImgLen = mic.pstImg_Data.length
          flag |= glocdef.GAMIC_ITEMFLAG_IMG
        }
      }

      mic.nItemFlag = flag.asInstanceOf[Byte] //传送的特征类型 ,特征+图像 , 1 2 4 8

      blob.getType match{
        case FPTProto.ImageType.IMAGETYPE_FINGER =>
          mic.nItemType = glocdef.GAMIC_ITEMTYPE_FINGER.asInstanceOf[Byte]
        case FPTProto.ImageType.IMAGETYPE_FACE =>
          mic.nItemType = glocdef.GAMIC_ITEMTYPE_FACE.asInstanceOf[Byte]
        case FPTProto.ImageType.IMAGETYPE_CARDIMG =>
          mic.nItemType = glocdef.GAMIC_ITEMTYPE_DATA.asInstanceOf[Byte]
        case FPTProto.ImageType.IMAGETYPE_PALM =>
          mic.nItemType = glocdef.GAMIC_ITEMTYPE_PALM.asInstanceOf[Byte]
        case FPTProto.ImageType.IMAGETYPE_VOICE =>
          mic.nItemType = glocdef.GAMIC_ITEMTYPE_VOICE.asInstanceOf[Byte]
        case other =>
          throw new UnsupportedOperationException
      }
      mic.bIsLatent = 1 //是否位现场数据

      data.pstMIC_Data = Array(mic)
      data.nMicItemCount = 1
    }
    data
  }
  //convert protocol string list as gafis GAKEYSTRUCT
  private def convertAsKeyArray(stringList:ProtocolStringList): Array[GAKEYSTRUCT] ={
    stringList.map{id=>
      val key = new GAKEYSTRUCT
      key.szKey = id
      key
    }.toArray
  }
  private[gloclib] def appendTextStruct(buffer:mutable.Buffer[GATEXTITEMSTRUCT],name:String,value:String):Unit = {
    if(value != null && value.length > 0) {
      val textStruct = new GATEXTITEMSTRUCT()
      textStruct.bIsPointer = 1
      textStruct.szItemName = name
      //convert as GBK encoding,because 6.2 need gbk encoding
      textStruct.stData.textContent = value.getBytes(AncientConstants.GBK_ENCODING)
      textStruct.nItemLen = textStruct.stData.textContent.length

      buffer += textStruct
    }

  }
  def convertProtobuf2GCASEINFOSTRUCT(protoCase:Case):GCASEINFOSTRUCT = {
    //TODO 添加数据长度校验
    val gafisCase = new GCASEINFOSTRUCT
    gafisCase.nItemFlag = (1 + 4 + 16).asInstanceOf[Byte]
    //GAFIS里面没有'A',这里去掉前缀
    gafisCase.szCaseID = protoCase.getStrCaseID
    if(gafisCase.szCaseID.charAt(0) == 'A')
      gafisCase.szCaseID = gafisCase.szCaseID.substring(1)

    gafisCase.pstFingerID_Data = convertAsKeyArray(protoCase.getStrFingerIDList)
    gafisCase.nFingerCount = gafisCase.pstFingerID_Data.length.asInstanceOf[Short]

    gafisCase.pstPalmID_Data= convertAsKeyArray(protoCase.getStrPalmIDList)
    gafisCase.nPalmCount = gafisCase.pstPalmID_Data.length.asInstanceOf[Short]


    if (protoCase.hasText) {
      val text = protoCase.getText
      val buffer = mutable.Buffer[GATEXTITEMSTRUCT]()

      appendTextStruct(buffer, "CaseClass1Code", text.getStrCaseType1)
      appendTextStruct(buffer, "CaseClass2Code", text.getStrCaseType2)
      appendTextStruct(buffer, "CaseClass3Code", text.getStrCaseType3)
      appendTextStruct(buffer, "SuspiciousArea1Code", text.getStrSuspArea1Code)
      appendTextStruct(buffer, "SuspiciousArea2Code", text.getStrSuspArea2Code)
      appendTextStruct(buffer, "SuspiciousArea3Code", text.getStrSuspArea3Code)
      appendTextStruct(buffer, "CaseOccurDate", text.getStrCaseOccurDate)
      appendTextStruct(buffer, "CaseOccurPlaceCode", text.getStrCaseOccurPlaceCode)
      appendTextStruct(buffer, "CaseOccurPlaceTail", text.getStrCaseOccurPlace)

      if(text.hasNSuperviseLevel)
        appendTextStruct(buffer, "SuperviseLevel", text.getNSuperviseLevel.toString)

      appendTextStruct(buffer, "ExtractUnitCode", text.getStrExtractUnitCode)
      appendTextStruct(buffer, "ExtractUnitNameTail", text.getStrExtractUnitName)
      appendTextStruct(buffer, "Extractor1", text.getStrExtractor)
      appendTextStruct(buffer, "ExtractDate", text.getStrExtractDate)
      appendTextStruct(buffer, "IllicitMoney", text.getStrMoneyLost)
      appendTextStruct(buffer, "Premium", text.getStrPremium)
      if(text.hasBPersonKilled)
        appendTextStruct(buffer, "HasPersonKilled", if (text.getBPersonKilled()) "1" else "0")
      appendTextStruct(buffer, "Comment", text.getStrComment)
      if(text.hasNCaseState)
        appendTextStruct(buffer, "CaseState", text.getNCaseState.toString)

      if(text.hasNXieChaState)
        appendTextStruct(buffer, "XieChaFlag", text.getNCaseState.toString)
      if(text.hasNCancelFlag)
        appendTextStruct(buffer, "CancelFlag", text.getNCancelFlag.toString)

      appendTextStruct(buffer, "XieChaDate", text.getStrXieChaDate)
      appendTextStruct(buffer, "XieChaRequestUnitName", text.getStrXieChaRequestUnitName)
      appendTextStruct(buffer, "XieChaRequestUnitCode", text.getStrXieChaRequestUnitCode)

      gafisCase.pstText_Data = buffer.toArray
      gafisCase.nTextItemCount = gafisCase.pstText_Data.length.asInstanceOf[Short]
    }
    gafisCase
  }
}
