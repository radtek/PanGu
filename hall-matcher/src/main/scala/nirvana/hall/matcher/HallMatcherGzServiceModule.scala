package nirvana.hall.matcher

import nirvana.hall.extractor.internal.FeatureExtractorImpl
import nirvana.hall.extractor.services.FeatureExtractor
import nirvana.hall.matcher.internal.adapter.common.{MatchTaskCronServiceImpl, PutMatchProgressServiceImpl}
import nirvana.hall.matcher.internal.adapter.gz.{GetMatchTaskServiceGzImpl, PutMatchResultServiceImpl, SyncDataServiceImpl}
import nirvana.hall.matcher.service._
import org.apache.tapestry5.ioc.ServiceBinder

/**
 * Created by songpeng on 16/3/25.
 */
object HallMatcherGzServiceModule {

  def bind(binder: ServiceBinder): Unit = {
    binder.bind(classOf[SyncDataService], classOf[SyncDataServiceImpl])
    binder.bind(classOf[GetMatchTaskService], classOf[GetMatchTaskServiceGzImpl])
    binder.bind(classOf[PutMatchResultService], classOf[PutMatchResultServiceImpl])
    binder.bind(classOf[PutMatchProgressService], classOf[PutMatchProgressServiceImpl])
    binder.bind(classOf[MatchTaskCronService], classOf[MatchTaskCronServiceImpl]).eagerLoad()
    //特征转换service
    binder.bind(classOf[FeatureExtractor], classOf[FeatureExtractorImpl])
  }

}