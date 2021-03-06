package nirvana.hall.v70

import nirvana.hall.api.services._
import nirvana.hall.api.services.sync.{FetchMatchRelationService, _}
import nirvana.hall.support.internal.RpcHttpClientImpl
import nirvana.hall.support.services.RpcHttpClient
import nirvana.hall.v70.internal._
import nirvana.hall.v70.internal.query.{Query7to6ServiceImpl, QueryGet7to6ServiceImpl}
import nirvana.hall.v70.internal.sync._
import nirvana.hall.v70.internal.sys.{DictServiceImpl, UserServiceImpl}
import nirvana.hall.v70.services.GetPKIDServiceImpl
import nirvana.hall.v70.services.query.{Query7to6Service, QueryGet7to6Service}
import nirvana.hall.v70.services.sys.{DictService, UserService}
import org.apache.tapestry5.ioc.ServiceBinder

/**
 * Created by songpeng on 16/1/25.
 */
object LocalV70ServiceModule {
  def bind(binder: ServiceBinder): Unit = {
    binder.bind(classOf[RpcHttpClient],classOf[RpcHttpClientImpl]).withId("RpcHttpClient")
    binder.bind(classOf[UserService], classOf[UserServiceImpl])
    binder.bind(classOf[DictService], classOf[DictServiceImpl])
    //api 接口实现类
    binder.bind(classOf[CaseInfoService], classOf[CaseInfoServiceImpl])
    binder.bind(classOf[LPCardService], classOf[LPCardServiceImpl])
    binder.bind(classOf[LPPalmService], classOf[LPPalmServiceImpl])
    binder.bind(classOf[TPCardService], classOf[TPCardServiceImpl])
    binder.bind(classOf[QueryService], classOf[QueryServiceImpl])
    binder.bind(classOf[MatchRelationService], classOf[MatchRelationServiceImpl])
    binder.bind(classOf[SyncInfoLogManageService], classOf[SyncInfoLogManageServiceImpl])
    binder.bind(classOf[AssistCheckRecordService],classOf[AssistCheckRecordServiceImpl])
    binder.bind(classOf[GetPKIDService], classOf[GetPKIDServiceImpl])
    binder.bind(classOf[ExportRelationService], classOf[ExportRelationServiceImpl])
    //同步
    binder.bind(classOf[FetchTPCardService], classOf[FetchTPCardServiceImpl])
    binder.bind(classOf[FetchLPCardService], classOf[FetchLPCardServiceImpl])
    binder.bind(classOf[FetchLPPalmService], classOf[FetchLPPalmServiceImpl])
    binder.bind(classOf[FetchCaseInfoService], classOf[FetchCaseInfoServiceImpl])
    binder.bind(classOf[FetchQueryService], classOf[FetchQueryServiceImpl])
    binder.bind(classOf[LogicDBJudgeService], classOf[LogicDBJudgeServiceImpl])
    binder.bind(classOf[FetchMatchRelationService],classOf[FetchMatchRelationServiceImpl])
    //定时服务
    binder.bind(classOf[Query7to6Service], classOf[Query7to6ServiceImpl]).eagerLoad()
    binder.bind(classOf[QueryGet7to6Service], classOf[QueryGet7to6ServiceImpl]).eagerLoad()
  }

}
