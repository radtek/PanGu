package nirvana.hall.v70.internal.filter.sys

import monad.rpc.protocol.CommandProto.BaseCommand
import monad.support.services.LoggerSupport
import nirvana.hall.api.services.{ProtobufRequestFilter, ProtobufRequestHandler}
import nirvana.hall.protocol.sys.SyncDictProto.{SyncDictRequest, SyncDictResponse}
import nirvana.hall.v70.services.sys.DictService

/**
 * Created by songpeng on 15/11/4.
 */
class SyncDictRequestFilter(syncDictService: DictService) extends ProtobufRequestFilter with LoggerSupport {
  override def handle(protobufRequest: BaseCommand, responseBuilder: BaseCommand.Builder, handler: ProtobufRequestHandler): Boolean = {
    if(protobufRequest.hasExtension(SyncDictRequest.cmd)){
      val request = protobufRequest.getExtension(SyncDictRequest.cmd)
      val response = SyncDictResponse.newBuilder().setDictType(request.getDictType)
      val dictList = syncDictService.findAllDict(request.getDictType)
      dictList.foreach(response.addDictData)
      responseBuilder.setExtension(SyncDictResponse.cmd, response.build())
      true
    } else {
      handler.handle(protobufRequest, responseBuilder)
    }
  }
}
