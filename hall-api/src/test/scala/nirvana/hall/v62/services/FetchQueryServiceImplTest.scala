package nirvana.hall.v62.services

import nirvana.hall.api.services.QueryService
import nirvana.hall.api.services.sync.FetchQueryService
import nirvana.hall.v62.BaseV62TestCase
import org.junit.{Assert, Test}

/**
 * Created by songpeng on 16/8/31.
 */
class FetchQueryServiceImplTest extends BaseV62TestCase{

  @Test
  def test_fetchMatchTask: Unit ={
    val service = getService[FetchQueryService]
    val matchTask = service.fetchMatchTask(10,"")
    matchTask.foreach{t => service.saveFetchRecord(t.getObjectId.toString)}
    Assert.assertNotNull(matchTask)
  }

  @Test
  def test_saveMatchResult: Unit ={
    val matchResult = getService[QueryService].getMatchResult(5).get
    val service = getService[FetchQueryService]
   /* service.saveMatchResult(matchResult, null)*/
  }
}
