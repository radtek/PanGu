package nirvana.hall.v62.internal.c.gnetlib

import monad.support.services.XmlLoader
import nirvana.hall.v62.config.HallV62Config
import nirvana.hall.v62.internal.V62Facade
import nirvana.hall.v62.internal.c.gloclib.BreakInfos
import org.junit.Test

/**
  * Created by songpeng on 2017/11/13.
  */
class gnetblobTest {
  val config = new HallV62Config
  config.appServer.host = "127.0.0.1"
  config.appServer.port = 6898
  config.appServer.user = "afisadmin"
  config.appServer.password="helloafis"
  val facade = new V62Facade(config)

  @Test
  def test_NET_GAFIS_COL_GetBySID: Unit ={
    val data = facade.NET_GAFIS_COL_GetBySID(V62Facade.DBID_TP_DEFAULT, V62Facade.TID_TPCARDINFO,1, "cardid")
    println(new String(data).trim)
  }
  @Test
  def test_NET_GAFIS_COL_GetByKey: Unit ={
    val data = facade.NET_GAFIS_COL_GetByKey(V62Facade.DBID_TP_DEFAULT, V62Facade.TID_TPCARDINFO,"1234567890", "hithistory")
    println(new String(data).trim)
    val breakInfos = XmlLoader.parseXML[BreakInfos](new String(data).trim)
    println(breakInfos.recordCount)
  }
  @Test
  def test_NET_GAFIS_COL_UpdateByKey: Unit ={
    val pD = "jcai".getBytes()
    facade.NET_GAFIS_COL_UpdateByKey(V62Facade.DBID_TP_DEFAULT, V62Facade.TID_TPCARDINFO,"1234567890", "name", pD)
  }
  @Test
  def test_NET_GAFIS_COL_UpdateBySID: Unit ={
    val pD = "jcai".getBytes()
    facade.NET_GAFIS_COL_UpdateBySID(V62Facade.DBID_TP_DEFAULT, V62Facade.TID_TPCARDINFO, 1, "name", pD)
  }
}
