package nirvana.hall.matcher.internal

import java.text.{ParsePosition, SimpleDateFormat}
import java.util.Date

import monad.support.services.LoggerSupport

/**
 * Created by songpeng on 16/6/13.
 */
object DateConverter extends LoggerSupport{

  /**
   * 将字符串转换为日期
   * @param str
   * @param format
   * @return
   */
  def convertStr2Date(str: String, format: String): Date ={
    var date:Date = null
    if(str != null && format != null){
      try{
        val formatter = new SimpleDateFormat(format)
        date = formatter.parse(str,new ParsePosition(0))
      }catch {
        case e: Exception=>
          error(e.getMessage, e)
          if(str.length == 10){
            val formatter = new SimpleDateFormat("yyyy-MM-dd");
            date = formatter.parse(str)
          }
      }
    }
    date
  }
}
