package nirvana.hall.v70.internal

import java.nio.ByteBuffer
import java.util.{Calendar, Date, UUID}

/**
 * Created by songpeng on 16/2/17.
 */
object CommonUtils {


  def getUUID(): String ={
    UUID.randomUUID().toString.replace("-","")
  }

  def int2Byte(num: Int): Array[Byte] = {
    ByteBuffer.allocate(4).putInt(num).array
  }
  def short2Byte(num: Short): Array[Byte] = {
    ByteBuffer.allocate(2).putShort(num).array
  }

  def getAFISDateTime (date: Date): Array[Byte] = {
    val c: Calendar = Calendar.getInstance
    c.setTime(date)
    val result: Array[Byte] = new Array[Byte](8)
    val ss: Array[Byte] = short2Byte((c.get(Calendar.SECOND) * 1000).toShort)
    result(0) = ss(1)
    result(1) = ss(0)
    result(2) = c.get(Calendar.MINUTE).toByte
    result(3) = c.get(Calendar.HOUR_OF_DAY).toByte
    result(4) = c.get(Calendar.DAY_OF_MONTH).toByte
    result(5) = c.get(Calendar.MONTH).toByte
    val yy: Array[Byte] = short2Byte(c.get(Calendar.YEAR).toShort)
    result(6) = yy(1)
    result(7) = yy(0)
    return result
  }
}
