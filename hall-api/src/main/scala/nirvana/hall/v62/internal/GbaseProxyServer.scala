package nirvana.hall.v62.internal

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}

import monad.support.services.{LoggerSupport, MonadException, MonadUtils}
import nirvana.hall.v62.config.V62ProxyBindSupport
import org.apache.tapestry5.ioc.services.RegistryShutdownHub
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.channel.{Channel, ChannelPipeline, ChannelPipelineFactory, Channels}

import scala.util.control.NonFatal

/**
  * gbase proxy server
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-04-27
  */
class GbaseProxyServer(rpcBindSupport:V62ProxyBindSupport) extends LoggerSupport {
  //一个主IO，2个worker
  val ioThread = rpcBindSupport.rpc.ioThread
  val workerThread = rpcBindSupport.rpc.workerThread
  val executor = Executors.newFixedThreadPool(ioThread + workerThread + 2, new ThreadFactory {
    private val seq = new AtomicInteger(0)

    override def newThread(r: Runnable): Thread = {
      val thread = new Thread(r)
      thread.setName("gbase-proxy-%s".format(seq.incrementAndGet()))
      thread.setDaemon(true)

      thread
    }
  })

  private var channelFactory: NioServerSocketChannelFactory = _
  private var bootstrap: ServerBootstrap = _
  private var serverChannel: Option[Channel] = None

  /**
    * 启动对象实例
    */
//  @PostConstruct
  def start(hub: RegistryShutdownHub) {

    channelFactory = new NioServerSocketChannelFactory(executor, executor, workerThread)
    bootstrap = new ServerBootstrap(channelFactory)
    bootstrap.setOption("child.tcpNoDelay", true)
    bootstrap.setOption("child.keepAlive", true)
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      def getPipeline: ChannelPipeline = {
        val pipeline = Channels.pipeline()
        //解码
        pipeline.addLast("frameDecoder", new GbasePkgFrameDecoder())
        pipeline.addLast("gbasePkgDecoder", new GbasePkgDecoder)

        //业务逻辑处理
        pipeline.addLast("handler", new GbasePackageHandler)
        pipeline
      }
    })
    serverChannel = Some(openOnce())
  }

  private def openOnce(): Channel = {
    try {
      val bindTuple = MonadUtils.parseBind(rpcBindSupport.rpc.bind)
      val address = new InetSocketAddress("0.0.0.0", bindTuple._2)
      bootstrap.bind(address)
    } catch {
      case NonFatal(e) =>
        shutdown()
        throw MonadException.wrap(e)
    }
  }

  def registryShutdownListener(hub: RegistryShutdownHub): Unit = {
    hub.addRegistryWillShutdownListener(new Runnable {
      override def run(): Unit = shutdown()
    })
  }

  /**
    * 关闭对象
    */
  def shutdown() {
    info("closing rpc server ...")
    serverChannel.foreach(_.close().awaitUninterruptibly())
    channelFactory.releaseExternalResources()
    MonadUtils.shutdownExecutor(executor, "rpc server executor service")
  }
}
