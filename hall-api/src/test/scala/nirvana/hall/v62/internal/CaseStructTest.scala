package nirvana.hall.v62.internal

import nirvana.hall.protocol.v62.FPTProto.Case
import org.junit.{Test, Assert}

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2015-11-03
 */
class CaseStructTest {
  @Test
  def test_case: Unit ={
    val struct = new tagGCASEINFOSTRUCT
    Assert.assertEquals(256,struct.getDataSize)
  }
  @Test
  def test_proto: Unit ={
    val protoCase = Case.newBuilder()
    protoCase.setStrCaseID("caseId")
    val textBuilder = protoCase.getTextBuilder
    textBuilder.setNCaseState(1)

    val gafisCase = CaseStruct.convertProtobuf2Case(protoCase.build())
    gafisCase.pstTextData.foreach(x=>println(x.szItemName))
    Assert.assertEquals(1,gafisCase.pstTextData.length)
    Assert.assertEquals(1,gafisCase.nTextItemCount)
  }
}
