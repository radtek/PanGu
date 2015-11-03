package nirvana.hall.v62.internal

import nirvana.hall.v62.services.{ChannelOperator, AncientClient}

/**
 * provide AncientClient instance
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2015-11-02
 */
trait AncientClientSupport {
  /**
   * obtain AncientClient instance
   * @return AncientClient instance
   */
  def createAncientClient:AncientClient
  def validateResponse(response: ResponseHeader,channel:ChannelOperator): Unit ={
    if(response.nReturnValue == -1) {
      val gafisError = channel.receive[GafisError]()
      throw new IllegalAccessException("fail to send data,num:%s".format(gafisError.nAFISErrno));
    }
  }
}
