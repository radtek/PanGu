package nirvana.hall.v70.internal

import nirvana.hall.protocol.api.FPTProto.TPCard
import nirvana.hall.v70.jpa.{GafisLogicDb, GafisLogicDbFingerprint, GafisLogicDbRule}
import org.junit.Test

/**
  * Created by Administrator on 2017/3/31.
  */
class LogicDBJudgeTest extends BaseV70TestCase {
  @Test
  def test:Unit = {
    //逻辑分库处理
    val logicDBJudge = new LogicDBJudgeServiceImpl
    //val logicDb: GafisLogicDb = logicDBJudge.logicTJudge(tpCard)

    val logicDbFingerprint = new GafisLogicDbFingerprint()
    val tpcard = TPCard.newBuilder()
    tpcard.setStrCardID("JLRY370233333333333333")
    var destDBT = logicDBJudge.logicJudge("JLRY370233333333333333",null,"0")
    var destDBL = logicDBJudge.logicJudge("370133333333333333",null,"1")
   // subcase2("370133333333333333")
  }

  def subcase1(tpCard:String):Unit = {
    //逻辑分库处理
    if (tpCard.startsWith("3702")) {
      print("青岛本地库")
      GafisLogicDb.find("1")
    } else if (tpCard.startsWith("BA") || tpCard.startsWith("JLRY")|| tpCard.startsWith("XCRY")) {
      print("社会人员库")
    } else if (tpCard.startsWith("37") && !tpCard.startsWith("3702")) {
      print("省厅库")
    } else {
      print("重点库")
    }
  }

  def subcase2(tpCard: String):Unit = {
    val logicDbRules = GafisLogicDbRule.find(GafisLogicDbRule.logicRule)

//    logicDbRules
//    for (logicDbRule <- logicDbRules) {
//      if (tpCard.matches(logicDbRule)) {
//        println(logicDbRule + "\t" + tpCard)
//      }
//    }
  }
}
