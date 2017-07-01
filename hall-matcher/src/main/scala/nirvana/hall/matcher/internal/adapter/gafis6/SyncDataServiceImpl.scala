package nirvana.hall.matcher.internal.adapter.gafis6

import javax.sql.DataSource

import monad.support.services.LoggerSupport
import nirvana.hall.matcher.config.HallMatcherConfig
import nirvana.hall.matcher.service.SyncDataService
import nirvana.protocol.NirvanaTypeDefinition.SyncDataType
import nirvana.protocol.SyncDataProto.{SyncDataRequest, SyncDataResponse}

/**
 * gafis6.2分库service
 */
class SyncDataServiceImpl(hallMatcherConfig: HallMatcherConfig, dataSource: DataSource)
  extends SyncDataService
  with LoggerSupport{
  /**
   * 同步数据
    *
    * @param syncDataRequest
   * @return
   */
  override def syncData(syncDataRequest: SyncDataRequest): SyncDataResponse = {
    val responseBuilder = SyncDataResponse.newBuilder
    responseBuilder.setSyncDataType(syncDataRequest.getSyncDataType)
    val size = syncDataRequest.getSize
    val timestamp = syncDataRequest.getTimestamp
    val syncDataType = syncDataRequest.getSyncDataType
    info("fetching data {} timestamp:{} size:{}", syncDataType,timestamp,size)
    val fetcher = syncDataType match {
//      case SyncDataType.PERSON => new sync.PersonFetcher(hallMatcherConfig, dataSource)
//      case SyncDataType.CASE => new sync.CaseFetcher(hallMatcherConfig, dataSource)
      case SyncDataType.TEMPLATE_FINGER => new sync.TemplateFingerFetcher(hallMatcherConfig, dataSource)
//      case SyncDataType.TEMPLATE_PALM => new sync.TemplatePalmFetcher(hallMatcherConfig, dataSource)
      case SyncDataType.LATENT_FINGER => new sync.LatentFingerFetcher(hallMatcherConfig, dataSource)
//      case SyncDataType.LATENT_PALM => new sync.LatentPalmFetcher(hallMatcherConfig, dataSource)
      case other => null
    }
    if(fetcher != null)
      fetcher.doFetch(responseBuilder, size, timestamp)
    info("{} data fetched with timestamp:{} size:{}",responseBuilder.getSyncDataCount,timestamp,size)
    responseBuilder.build()
  }

}