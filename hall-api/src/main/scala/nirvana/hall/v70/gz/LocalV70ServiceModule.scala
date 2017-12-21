package nirvana.hall.v70.gz

import nirvana.hall.api.internal.fpt.exchange.FPTExchangeServiceImpl
import nirvana.hall.api.services._
import nirvana.hall.api.services.fpt.exchange.FPTExchangeService
import nirvana.hall.api.services.sync.{FetchMatchRelationService, _}
import nirvana.hall.support.internal.RpcHttpClientImpl
import nirvana.hall.support.services.RpcHttpClient
import nirvana.hall.v70.internal.ExceptRelationServiceImpl
//import nirvana.hall.v70.gz.services._
import nirvana.hall.v70.gz.services.versionfpt5._
import nirvana.hall.v70.gz.sys.{DictService, DictServiceImpl, UserService, UserServiceImpl}
import nirvana.hall.v70.internal.sync.{FetchCaseInfoServiceImpl, FetchLPCardServiceImpl, FetchLPPalmServiceImpl, FetchMatchRelationServiceImpl, FetchQueryServiceImpl, FetchTPCardServiceImpl}
import nirvana.hall.v70.internal.{CaseInfoServiceImpl => _, LPCardServiceImpl => _, TPCardServiceImpl => _}
import nirvana.hall.v70.services.GetPKIDServiceImpl
import org.apache.tapestry5.ioc.ServiceBinder

/**
  * Created by songpeng on 2017/5/26.
  */
object LocalV70ServiceModule {

  def bind(binder: ServiceBinder): Unit = {
    binder.bind(classOf[RpcHttpClient], classOf[RpcHttpClientImpl]).withId("RpcHttpClient")
    binder.bind(classOf[UserService], classOf[UserServiceImpl])
    binder.bind(classOf[DictService], classOf[DictServiceImpl])
    //api 接口实现类
    binder.bind(classOf[TPCardService], classOf[TPCardServiceImpl])
    //由于依赖关系,这里暂时复用v70，如果有需要单独实现
    binder.bind(classOf[CaseInfoService], classOf[CaseInfoServiceImpl])
    binder.bind(classOf[LPCardService], classOf[LPCardServiceImpl])
    binder.bind(classOf[LPPalmService], classOf[LPPalmServiceImpl])
    binder.bind(classOf[GetPKIDService], classOf[GetPKIDServiceImpl])
    binder.bind((classOf[FPTExchangeService]),classOf[FPTExchangeServiceImpl])
    binder.bind(classOf[ExceptRelationService], classOf[ExceptRelationServiceImpl])

    //同步
    binder.bind(classOf[FetchTPCardService], classOf[FetchTPCardServiceImpl])
    binder.bind(classOf[FetchLPCardService], classOf[FetchLPCardServiceImpl])
    binder.bind(classOf[FetchLPPalmService], classOf[FetchLPPalmServiceImpl])
    binder.bind(classOf[FetchCaseInfoService], classOf[FetchCaseInfoServiceImpl])
    binder.bind(classOf[FetchQueryService], classOf[FetchQueryServiceImpl])
    binder.bind(classOf[FetchMatchRelationService],classOf[FetchMatchRelationServiceImpl])
  }
}
