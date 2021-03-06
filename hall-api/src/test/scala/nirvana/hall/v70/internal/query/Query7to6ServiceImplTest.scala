package nirvana.hall.v70.internal.query

import nirvana.hall.v70.internal.BaseV70TestCase
import nirvana.hall.v70.services.query.Query7to6Service
import org.junit.{Assert, Test}

/**
 * Created by songpeng on 15/12/28.
 */
class Query7to6ServiceImplTest extends BaseV70TestCase{
  @Test
  def test_sendQuery(): Unit ={
    val service = getService[Query7to6Service]

    val task = service.getGafisNormalqueryQueryqueWait

    Assert.assertNotNull(task)

    service.sendQuery(task.get);

  }
//  def test_sendQuery2():Unit = {
//    val service = getService[Query7to6Service]
//
//    service.getGafisNormalqueryQueryqueWait.foreach()
//
//    Assert.assertNotNull(task)
//
//    service.sendQuery(task.get);
  //}
}
