package nirvana.hall.v62.internal

import monad.support.services.LoggerSupport
import nirvana.hall.v62.services.AncientEnum.MatchType
import nirvana.hall.v62.services.{AncientClient, MatchOptions, DatabaseTable, SelfMatchTask}
import org.junit.{Assert, Test}

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2015-11-02
 */
class SendMatchTaskSupportTest extends LoggerSupport{

  @Test
  def test_query_match_result: Unit ={
    val sender = createSender()
    sender.queryMatchResult(7)
  }
  @Test
  def test_send: Unit ={
    val srcDb = DatabaseTable(1,2)
    val destDb = DatabaseTable(1,2)
    val options = new MatchOptions
    options.matchType = MatchType.TT
    options.positions = Array[Int](1,2,3,4,5,6,7,8,9,10)
    options.srcDb = srcDb
    options.destDb = destDb

    val task = SelfMatchTask("3702022014000002",options)

    val sender = createSender()


    val sid = sender.sendMatchTask(task)
    debug("sid :{}",sid)
    Assert.assertTrue(sid.head > 0)
  }
  private def createSender():SendMatchTaskSupport={
    new SendMatchTaskSupport with AncientClientSupport with LoggerSupport{
      /**
       * obtain AncientClient instance
       * @return AncientClient instance
       */
      override def createAncientClient: AncientClient = {
        AncientAppClient.connect("10.1.6.182",6798)
      }
    }
  }
}
