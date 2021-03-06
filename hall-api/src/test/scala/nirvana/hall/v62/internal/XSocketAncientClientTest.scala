package nirvana.hall.v62.internal


import java.nio.ByteBuffer

import nirvana.hall.c.services.AncientData
import nirvana.hall.c.services.gloclib.glocndef.GNETANSWERHEADOBJECT
import org.jboss.netty.buffer.ChannelBuffers
import org.junit.{Assert, Test}

import scala.reflect._
import scala.reflect.runtime.universe._

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2015-11-12
 */
class XSocketAncientClientTest {
  def getType[T: TypeTag](obj: T) = typeOf[T]
  def getClassTag[T: ClassTag](obj: T) = classTag[T]
  @Test
  def test_buffer: Unit ={
    val buffers = Seq(ByteBuffer.allocate(10),ByteBuffer.allocate(100))
    val buffer = ChannelBuffers.wrappedBuffer(buffers:_*)
    val destBuffer = new Array[Byte](110)
    buffer.readBytes(destBuffer)
  }
  @Test
  def test_receive: Unit ={
    val myType = typeOf[this.type]
    println(myType)
    val tpe  = getType(this)
    getClassTag(tpe)
    println(getType(this))
    Assert.assertNull(receive())
    Assert.assertNotNull(receive[GNETANSWERHEADOBJECT]())
  }
  def receive[R <: AncientData :ClassTag](): R = {
    classTag[R] match{
      case t if t == classTag[Nothing] =>
        null.asInstanceOf[R]
      case _ =>
        classTag[R].runtimeClass.newInstance().asInstanceOf[R]
    }
  }
}
