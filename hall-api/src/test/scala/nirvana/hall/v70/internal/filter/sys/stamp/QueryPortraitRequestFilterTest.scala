package nirvana.hall.v70.internal.filter.sys.stamp

import monad.rpc.protocol.CommandProto.{CommandStatus, BaseCommand}
import nirvana.hall.api.internal.BaseServiceTestSupport
import nirvana.hall.api.services.ProtobufRequestHandler
import nirvana.hall.protocol.sys.stamp.QueryPortraitProto.{QueryPortraitResponse, QueryPortraitRequest}
import nirvana.hall.protocol.sys.stamp.SavePersonProto.{SavePersonResponse, SavePersonRequest}
import org.junit.{Assert, Test}

/**
 * Created by wangjue on 2015/11/17.
 */
class QueryPortraitRequestFilterTest extends BaseServiceTestSupport{

  @Test
  def test_queryPortrait: Unit ={
    val addRequest = SavePersonRequest.newBuilder()
    addRequest.setPersonInfo("personid=CS520201511050001&yy=test&name=&idcardno=&gatherDate=2015-11-9 15:53:30&dataSources=1&gatherFingerNum=10")

    val handler = registry.getService(classOf[ProtobufRequestHandler])

    //add person
    val protobufRequest = BaseCommand.newBuilder().setTaskId(1)
    protobufRequest.setExtension(SavePersonRequest.cmd, addRequest.build())
    val protobufResponse = BaseCommand.newBuilder()
    protobufResponse.setStatus(CommandStatus.OK)
    handler.handle(protobufRequest.build(), protobufResponse)
    val personid = protobufResponse.getExtension(SavePersonResponse.cmd).getPersonInfo(0).getPersonid
    Assert.assertEquals("CS520201511050001",personid)

    //add portrait

    //query
    val queryRequest = QueryPortraitRequest.newBuilder()
    queryRequest.setPersonId("CS520201511050001")
    val protobufRequest1 = BaseCommand.newBuilder().setTaskId(1)
    protobufRequest1.setExtension(QueryPortraitRequest.cmd, queryRequest.build())
    val protobufResponse1 = BaseCommand.newBuilder()
    protobufResponse1.setStatus(CommandStatus.OK)
    handler.handle(protobufRequest1.build(), protobufResponse1)
    val pid = protobufResponse.getExtension(QueryPortraitResponse.cmd).getPortraitInfo(0).getPersonid
    val fgp = protobufResponse.getExtension(QueryPortraitResponse.cmd).getPortraitInfo(0).getFgp
    val gatherData = protobufResponse.getExtension(QueryPortraitResponse.cmd).getPortraitInfo(0).getGatherData
    Assert.assertEquals("1",fgp)

  }

}
