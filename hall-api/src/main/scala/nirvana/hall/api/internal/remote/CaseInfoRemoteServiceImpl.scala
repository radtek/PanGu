package nirvana.hall.api.internal.remote

import monad.support.services.LoggerSupport
import nirvana.hall.api.services.remote.CaseInfoRemoteService
import nirvana.hall.protocol.api.CaseProto._
import nirvana.hall.protocol.api.FPTProto.Case
import nirvana.hall.support.services.RpcHttpClient

/**
 * Created by songpeng on 16/3/7.
 */
class CaseInfoRemoteServiceImpl(rpcHttpClient: RpcHttpClient) extends CaseInfoRemoteService with LoggerSupport{
  /**
   * 获取案件信息
   * @param caseId
   * @param url
   * @return
   */
  override def getCaseInfo(caseId: String, url: String, dbId: String, headerMap: Map[String, String]): Option[Case] = {
    info("remote get caseInfo [caseId:{},url:{}]", caseId, url)
    val request = CaseGetRequest.newBuilder().setCaseId(caseId)
    request.setDbid(dbId)
    val response = rpcHttpClient.call(url, CaseGetRequest.cmd, request.build(),headerMap)
    val caseInfo = response.getExtension(CaseGetResponse.cmd).getCase

    if(caseInfo.getStrCaseID.nonEmpty){
      Option(caseInfo)
    }else{
      None
    }
  }

  /**
   * 添加案件信息
   * @param caseInfo
   * @return
   */
  override def addCaseInfo(caseInfo: Case, url: String, dbId: String, headerMap: Map[String, String]) = {
    info("remote add caseInfo [caseId:{},url:{}]", caseInfo.getStrCaseID, url)
    val request = CaseAddRequest.newBuilder().setCase(caseInfo)
    request.setDbid(dbId)
    rpcHttpClient.call(url, CaseAddRequest.cmd, request.build())
  }

  /**
   * 更新案件信息
   * @param caseInfo
   * @param url
   */
  override def updateCaseInfo(caseInfo: Case, url: String, dbId: String, headerMap: Map[String, String]): Unit = {
    info("remote update caseInfo [caseId:{},url:{}]", caseInfo.getStrCaseID, url)
    val request = CaseUpdateRequest.newBuilder().setCase(caseInfo)
    request.setDbid(dbId)
    rpcHttpClient.call(url, CaseUpdateRequest.cmd, request.build())
  }

  /**
   * 删除案件信息
   * @param caseId
   * @param url
   */
  override def deleteCaseInfo(caseId: String, url: String, dbId: String, headerMap: Map[String, String]): Unit = {
    info("remote delete caseInfo [caseId:{},url:{}]", caseId, url)
    val request = CaseDelRequest.newBuilder().setCaseId(caseId)
    request.setDbid(dbId)
    rpcHttpClient.call(url, CaseDelRequest.cmd, request.build())
  }
  /**
   * 案件编号是否存在
   * @param caseId
   * @param url
   * @param dbId
   */
  override def isExist(caseId: String, url: String, dbId: String, headerMap: Map[String, String]): Boolean = {
    val request = CaseIsExistRequest.newBuilder()
    request.setCardId(caseId)
    request.setDbid(dbId)
    val baseResponse = rpcHttpClient.call(url, CaseIsExistRequest.cmd, request.build())
    val response = baseResponse.getExtension(CaseIsExistResponse.cmd)

    response.getIsExist
  }

}
