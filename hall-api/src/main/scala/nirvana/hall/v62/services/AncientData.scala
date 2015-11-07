package nirvana.hall.v62.services

import nirvana.hall.v62.annotations.{IgnoreTransfer, Length}
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}

import scala.reflect.runtime._
import scala.reflect.runtime.universe._

/**
 * support to serialize/unserialize data
 * TODO using byte transformation to convert data
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2015-10-29
 */
object AncientData {
  val mirror = universe.runtimeMirror(getClass.getClassLoader)
}
sealed trait ScalaReflect{
  private var dataSize:Int = 0
  private def findBaseLength(tpe:Type,length:Int):Int={
    val BYTE_CLASS = typeOf[Byte]
    val CHAR_CLASS = typeOf[Char]
    val SHORT_CLASS = typeOf[Short]
    val INT_CLASS = typeOf[Int]
    val LONG_CLASS = typeOf[Long]
    val STRING_CLASS = typeOf[String]
    val ANCIENT_DATA_TYPE = typeOf[AncientData]
    tpe match {
      case `BYTE_CLASS` | `CHAR_CLASS` =>
        1
      case `SHORT_CLASS` =>
        2
      case `INT_CLASS` =>
        4
      case `LONG_CLASS` =>
        8
      case `STRING_CLASS` =>
        if(length == 0){
          throw new IllegalArgumentException("@Lenght not defined "+tpe)
        }
        length
      case other =>
        if (other <:< typeOf[AncientData]) {
          val ct = other.typeSymbol.asClass.primaryConstructor.asMethod
          AncientData.mirror.reflectClass(other.typeSymbol.asClass).reflectConstructor(ct)().asInstanceOf[AncientData].getDataSize
        }else {
          if(length == 0){
            throw new IllegalArgumentException("@Lenght not defined "+tpe)
          }
          length * tpe.typeArgs.map(findBaseLength(_,0)).sum
        }
    }
  }
  def getDataSizeByScala:Int={
    if(dataSize == 0) {
      val instanceMirror = AncientData.mirror.reflect(this)
      val decl = instanceMirror.symbol.asType.toType
      decl.members
        .filter(_.isTerm)
        .filter(_.asTerm.isVar)
        .filterNot(_.annotations.exists (typeOf[IgnoreTransfer] =:= _.tree.tpe))
        .foreach { m =>
        val lengthAnnotation = m.annotations.find (typeOf[Length] =:= _.tree.tpe)
        val length = lengthAnnotation.map(_.tree.children.tail.head.children(1).asInstanceOf[Literal].value.value.asInstanceOf[Int]).sum
        dataSize += findBaseLength(m.info,length)
      }
    }

    dataSize
  }
}
sealed trait JavaReflect{
  private var dataSize:Int = 0
  /**
   * get data size
   */
  def getDataSize:Int={
    if(dataSize == 0) {
      val BYTE_CLASS = classOf[Byte]
      val CHAR_CLASS = classOf[Char]
      val SHORT_CLASS = classOf[Short]
      val INT_CLASS = classOf[Int]
      val LONG_CLASS = classOf[Long]
      val STRING_CLASS = classOf[String]
      val BYTE_ARRAY_CLASS = classOf[Array[Byte]]
      val ANCIENT_CLASS = classOf[AncientData]
      getClass.getDeclaredFields.filterNot(_.isAnnotationPresent(classOf[IgnoreTransfer])).foreach { f =>
        val annotation = f.getAnnotation(classOf[Length])
        var dataLength = 0
        if (annotation != null) {
          dataLength = annotation.value()
        }
        f.getType match {
          case `BYTE_CLASS` =>
            dataSize += 1
          case `CHAR_CLASS` =>
            dataSize += 1
          case `SHORT_CLASS` =>
            dataSize += 2
          case `INT_CLASS` =>
            dataSize += 4
          case `LONG_CLASS` =>
            dataSize += 8
          case `STRING_CLASS` =>
            dataSize += dataLength
          case `BYTE_ARRAY_CLASS` =>
            dataSize += dataLength
          case other =>
            if(ANCIENT_CLASS.isAssignableFrom(other)) {
              dataSize += other.newInstance().asInstanceOf[AncientData].getDataSize
            }else if(other.isArray){
              val componentType = other.getComponentType
              if(ANCIENT_CLASS.isAssignableFrom(componentType)){
                dataSize += componentType.newInstance().asInstanceOf[AncientData].getDataSize * dataLength
              }else{
                throw new IllegalAccessException("unknown type:" + componentType)
              }
            }else {
              throw new IllegalAccessException("unknown type:" + other)
            }
        }
      }
    }
    dataSize
  }
  /**
   * serialize to channel buffer
   * @param channelBuffer netty channel buffer
   */
  def writeToChannelBuffer(channelBuffer:ChannelBuffer): ChannelBuffer = {
    getClass.getDeclaredFields.filterNot(_.isAnnotationPresent(classOf[IgnoreTransfer])).foreach { f =>
      val annotation = f.getAnnotation(classOf[Length])
      val fieldValue = getClass.getMethod(f.getName).invoke(this)
      var dataLength = 0
      if(annotation != null){
        dataLength = annotation.value()
      }
      val BYTE_CLASS         = classOf[Byte]
      val CHAR_CLASS         = classOf[Char]
      val SHORT_CLASS        = classOf[Short]
      val INT_CLASS          = classOf[Int]
      val LONG_CLASS         = classOf[Long]
      val STRING_CLASS       = classOf[String]
      val BYTE_ARRAY_CLASS   = classOf[Array[Byte]]
      val ANCIENT_CLASS      = classOf[AncientData]
      f.getType match{
        case `BYTE_CLASS` =>
          channelBuffer.writeByte(fieldValue.asInstanceOf[Byte])
        case `CHAR_CLASS` =>
          channelBuffer.writeChar(fieldValue.asInstanceOf[Char])
        case `SHORT_CLASS` =>
          channelBuffer.writeShort(fieldValue.asInstanceOf[Short])
        case `INT_CLASS` =>
          channelBuffer.writeInt(fieldValue.asInstanceOf[Int])
        case `LONG_CLASS` =>
          channelBuffer.writeLong(fieldValue.asInstanceOf[Long])
        case `STRING_CLASS` =>
          val x = fieldValue.asInstanceOf[String]
          var length = 0
          if(x!= null) {
            val bytes = x.getBytes
            length = bytes.length
            if (length > dataLength) {
              throw new IllegalAccessException("value length[" ++ "] > length defined [" + dataLength + "]")
            }
            channelBuffer.writeBytes(bytes)
          }
          channelBuffer.writerIndex(channelBuffer.writerIndex() + dataLength - length)
        case `BYTE_ARRAY_CLASS` =>
          var length = 0
          val x = fieldValue.asInstanceOf[Array[Byte]]
          if(x!= null) {
            length = x.length
            if (length > dataLength) {
              throw new IllegalAccessException("value length[" ++ "] > length defined [" + dataLength + "]")
            }
            channelBuffer.writeBytes(x)
          }
          channelBuffer.writerIndex(channelBuffer.writerIndex() + dataLength - length)

        case `ANCIENT_CLASS` =>
          fieldValue.asInstanceOf[AncientData].writeToChannelBuffer(channelBuffer)
        case other=>
          if(ANCIENT_CLASS.isAssignableFrom(other)) {
            if(fieldValue == null){
              val size = other.newInstance().asInstanceOf[AncientData].getDataSize
              channelBuffer.writerIndex(channelBuffer.writerIndex() + size)
            }else{
              fieldValue.asInstanceOf[AncientData].writeToChannelBuffer(channelBuffer)
            }
          }else if(other.isArray){
            val componentType = other.getComponentType
            if(ANCIENT_CLASS.isAssignableFrom(componentType)){
              val componentSize = componentType.newInstance().asInstanceOf[AncientData].getDataSize
              if(fieldValue == null){//fill empty data
                channelBuffer.writerIndex(channelBuffer.writerIndex() + componentSize * dataLength)
              }else{
                val arr = fieldValue.asInstanceOf[Array[_]]
                arr.foreach { x =>
                  if(x == null)//increase writer index
                    channelBuffer.writerIndex(channelBuffer.writerIndex() + componentSize)
                  else
                    x.asInstanceOf[AncientData].writeToChannelBuffer(channelBuffer)
                }

                val remain = dataLength - arr.length
                if(remain > 0)
                  channelBuffer.writerIndex(channelBuffer.writerIndex() + componentSize * remain)
              }
            }else{
              throw new IllegalAccessException("unknown type:" + componentType)
            }
          }else {
            throw new IllegalAccessException("unknown type:" + other)
          }
      }
    }

    channelBuffer
  }
  def fromBytes(bytes: Array[Byte]): this.type ={
    fromChannelBuffer(ChannelBuffers.wrappedBuffer(bytes))
  }

  /**
   * convert channel buffer data as object
   * @param channelBuffer netty channel buffer
   */
  def fromChannelBuffer(channelBuffer: ChannelBuffer): this.type ={
    getClass.getDeclaredFields.filterNot(_.isAnnotationPresent(classOf[IgnoreTransfer])).foreach { f =>
      val annotation = f.getAnnotation(classOf[Length])
      var dataLength = 0
      if(annotation != null){
        dataLength = annotation.value()
      }
      val BYTE_CLASS         = classOf[Byte]
      val CHAR_CLASS         = classOf[Char]
      val SHORT_CLASS        = classOf[Short]
      val INT_CLASS          = classOf[Int]
      val LONG_CLASS         = classOf[Long]
      val STRING_CLASS       = classOf[String]
      val BYTE_ARRAY_CLASS   = classOf[Array[Byte]]
      val ANCIENT_CLASS      = classOf[AncientData]
      f.getType match{
        case `BYTE_CLASS` =>
          getClass.getMethod(f.getName+"_$eq",BYTE_CLASS).invoke(this,channelBuffer.readByte().asInstanceOf[AnyRef])
        case `CHAR_CLASS` =>
          getClass.getMethod(f.getName+"_$eq",CHAR_CLASS).invoke(this,channelBuffer.readChar().asInstanceOf[AnyRef])
        case `SHORT_CLASS` =>
          getClass.getMethod(f.getName+"_$eq",SHORT_CLASS).invoke(this,channelBuffer.readShort().asInstanceOf[AnyRef])
        case `INT_CLASS` =>
          getClass.getMethod(f.getName+"_$eq",INT_CLASS).invoke(this,channelBuffer.readInt().asInstanceOf[AnyRef])
        case `LONG_CLASS` =>
          getClass.getMethod(f.getName+"_$eq",LONG_CLASS).invoke(this,channelBuffer.readLong().asInstanceOf[AnyRef])
        case `STRING_CLASS` =>
          val bytes = new Array[Byte](dataLength)
          channelBuffer.readBytes(bytes)
          getClass.getMethod(f.getName+"_$eq",STRING_CLASS).invoke(this,new String(bytes).trim)
        case `BYTE_ARRAY_CLASS` =>
          val bytes = new Array[Byte](dataLength)
          channelBuffer.readBytes(bytes)
          getClass.getMethod(f.getName+"_$eq",BYTE_ARRAY_CLASS).invoke(this,bytes)
        case `ANCIENT_CLASS` =>
          val ancientData = f.getType.newInstance().asInstanceOf[AncientData]
          ancientData.fromChannelBuffer(channelBuffer)
          getClass.getMethod(f.getName+"_$eq",f.getType).invoke(this,ancientData)
        case other=>
          if(ANCIENT_CLASS.isAssignableFrom(other)) {
            val ancientData = other.newInstance().asInstanceOf[AncientData]
            ancientData.fromChannelBuffer(channelBuffer)
            getClass.getMethod(f.getName+"_$eq",other).invoke(this,ancientData)
          }else if(other.isArray){
            val componentType = other.getComponentType
            if(ANCIENT_CLASS.isAssignableFrom(componentType)){
              val dataArray = java.lang.reflect.Array.newInstance(componentType,dataLength)

              0 until dataLength foreach { x=>
                val value = componentType.newInstance().asInstanceOf[AncientData]
                value.fromChannelBuffer(channelBuffer)
                java.lang.reflect.Array.set(dataArray,x,value)
              }
              getClass.getMethod(f.getName+"_$eq",other).invoke(this,dataArray)
            }else{
              throw new IllegalAccessException("unknown type:" + componentType)
            }
          }else {
            throw new IllegalAccessException("unknown type:" + other)
          }
      }
    }

    this
  }
}
class AncientData extends JavaReflect{
}

class DynamicByteArray(val data:Array[Byte]) extends AncientData{
  /**
   * convert channel buffer data as object
   * @param channelBuffer netty channel buffer
   */
  override def fromChannelBuffer(channelBuffer: ChannelBuffer): this.type = {
    if(channelBuffer.readableBytes() >= data.length){
      channelBuffer.readBytes(data)
    }
    this
  }

  /**
   * get data size
   */
  override def getDataSize: Int = data.length

  /**
   * serialize to channel buffer
   * @param channelBuffer netty channel buffer
   */
  override def writeToChannelBuffer(channelBuffer: ChannelBuffer): ChannelBuffer = {
    channelBuffer.writeBytes(data)
    channelBuffer
  }
}
