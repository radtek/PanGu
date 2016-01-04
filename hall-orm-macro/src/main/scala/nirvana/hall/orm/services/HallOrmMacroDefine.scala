package nirvana.hall.orm.services


import scala.collection.mutable
import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * hall orm macro definition
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-01
 */
object HallOrmMacroDefine {
  private val find_by_pattern = "(?i)find_by_([_a-zA-Z0-9]*)".r
  case class SimpleQuery[A](ql:String, params:Seq[Any])
  case class SimpleOrder[A](order:String)
  case class NamedParameter(key:String,value:Any)

  /**
   * find method
   */
  def findDynamicImpl[E: c.WeakTypeTag,R](c: whitebox.Context)
                                       (name: c.Expr[String])
                                       (params:c.Expr[Any]*) : c.Expr[R] = {
    import c.universe._
    val Literal(Constant(findMethod)) = name.tree
    val paramsTree = params.map(_.tree).toList
    findMethod.toString.trim match{
      case find_by_pattern(attributes) =>
        val clazzName = c.weakTypeOf[E]
        var ql = "from "+clazzName+" where 1=1"
        //find class field
        val expectedNames =  c.weakTypeOf[E].members
          .filter(_.isTerm)
          .filter(_.asTerm.isVar).map(_.name.toString.trim).toSeq
        val attrs =  attributes.split("_and_")
        if(attrs.length != params.length){
          c.error(c.enclosingPosition, s"name's length ${attrs.length} !=  parameter's length ${params.length}.")
        }
        attrs.zipWithIndex.foreach{case (attr,index)=>
          if(expectedNames.contains(attr)){
            ql += " and %s=?%s".format(attr,index+1)
          }else{
            c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#${attr} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
          }
        }
        val qlTree = Literal(Constant(ql))

        c.Expr[R](Apply(Apply(Select(c.prefix.tree, TermName("internalWhere")), List(qlTree)),paramsTree))
      case "where" =>
        val qlTree = paramsTree.head
        val remainTree = paramsTree.drop(1)
        c.Expr[R](Apply(Apply(Select(c.prefix.tree, TermName("internalWhere")), List(qlTree)),remainTree))
      case other=>
        c.error(c.enclosingPosition, s"unsupport operation")
        c.Expr[R](Literal(Constant(Nil)))
    }

  }
  def dslDynamicImplNamed[E: c.WeakTypeTag,R](c: whitebox.Context)
                                           (name: c.Expr[String])
                                           (params:c.Expr[(String,Any)]*):c.Expr[R] = {
    import c.universe._
    val Literal(Constant(methodName:String)) = name.tree
    //find class field
    val expectedNames =  c.weakTypeOf[E].members
      .filter(_.isTerm)
      .filter(_.asTerm.isVar).map(_.name.toString.trim).toSeq
    //validate params
    var order_str=""

    val trees = params.map(_.tree).toList
    trees.foreach{
      case Apply(_,Literal(Constant(_name: String))::_) =>
        if(_name.isEmpty)
          c.error(c.enclosingPosition, s"name parameter is empty.")
        else if(!expectedNames.contains(_name))
          c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#${_name} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
      case other =>
        c.error(c.enclosingPosition, s"${other} unsupported.")
    }
    //c.error(c.enclosingPosition,"asdf")

    methodName match{
      case "order" | "order_by" =>
        c.Expr[R](Apply(Select(c.prefix.tree, TermName("internalOrder")), trees))
      case other=>
        c.error(c.enclosingPosition, s"${other} unsupported.")
        c.Expr[R](Literal(Constant(Nil)))
    }
  }
  def findDynamicImplNamed[E: c.WeakTypeTag,R](c: whitebox.Context)
                                            (name: c.Expr[String])
                                            (params:c.Expr[(String,Any)]*): c.Expr[R] = {
    import c.universe._

    val clazzName = c.weakTypeOf[E]
    var ql = "from "+clazzName+" where 1=1"

    val Literal(Constant(methodName:String)) = name.tree
    //find class field
    val expectedNames =  c.weakTypeOf[E].members
      .filter(_.isTerm)
      .filter(_.asTerm.isVar).map(_.name.toString.trim).toSeq

    //validate params
    val trees = params.map(_.tree).toList
    val parameterValues = mutable.Buffer[Tree]()
    var i = 1
    trees.foreach {
      case Apply(_,Literal(Constant(_name: String))::value::Nil) =>
        if(_name.isEmpty)
          c.error(c.enclosingPosition, s"name parameter is empty.")
        else if(!expectedNames.contains(_name))
          c.error(c.enclosingPosition, s"${c.weakTypeOf[E]}#${_name} not found. Expected fields are ${expectedNames.mkString("#", ", #", "")}.")
        else {
          ql += " and %s=?%s".format(_name,i)
          i += 1
          parameterValues += value
        }
      case other =>
          c.error(c.enclosingPosition, s"${other} unsupported.")
    }

    methodName match{
      case "find_by" | "where" =>
        c.Expr[R](Apply(Apply(Select(c.prefix.tree, TermName("internalWhere")), List(Literal(Constant(ql)))),parameterValues.toList))
      case other=>
        c.error(c.enclosingPosition, s"${other} unsupported.")
        c.Expr[R](Literal(Constant(Nil)))
    }
  }
}
