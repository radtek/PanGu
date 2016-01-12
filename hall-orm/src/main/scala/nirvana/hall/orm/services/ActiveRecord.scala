package nirvana.hall.orm.services

import javax.persistence.criteria.{CriteriaBuilder, CriteriaQuery, Predicate}
import javax.persistence.{EntityManager, Id, Transient}

import org.apache.tapestry5.ioc.ObjectLocator
import org.slf4j.LoggerFactory

import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.Stream
import scala.collection.{GenTraversableOnce, mutable}
import scala.language.{postfixOps, dynamics}
import scala.language.experimental.macros
import scala.reflect.{ClassTag, classTag}

/**
 * active record
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
object ActiveRecord {
  //logger
  private val logger = LoggerFactory getLogger getClass
  @volatile
  var objectLocator:ObjectLocator= _

  case object NotNull
  case object Null
  case class Between(start:Any,end:Any)
  case class Not(value:Any)

  /**
   * Saves the model.
   * If the model is new a record gets created in the database,otherwise
   * the existing record gets updated.
   */
  def save[T](record:T):T={
    getService[EntityService].save(record)
  }

  /**
   * Deletes the record in the database
   * The row is simply removed with an SQL +DELETE+ statement on the
   * record's primary key.
   */
  def delete[T](record:T): Unit ={
    getService[EntityService].delete(record)
  }
  def updateRelation[T](relation: Relation[T]):Int={
    getService[EntityService].updateRelation(relation)
  }
  def deleteRelation[T](relation: Relation[T]):Int={
    getService[EntityService].deleteRelation(relation)
  }

  /**
   * find some records by Relation
   * @param relation relation object
   * @tparam T type parameter
   * @return record stream
   */
  private[services] def find[T](relation:QuerySupport[T]):Stream[T]={
    getService[EntityService].find(relation)
  }

  /**
   * find some service using ObjectLocator
   */
  private[orm] def getService[T:ClassTag]:T={
    if(objectLocator == null)
      throw new IllegalStateException("object locator is null")
    objectLocator.getService(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }
  /**
   * find_by and where function
   * @param ql query
   * @param params parameter values
   * @return Relation object
   */
  def internalWhere[A](clazz:Class[A],primaryKey:String,ql:String)(params:Any*): Relation[A]={
    new Relation[A](clazz,primaryKey,ql,params)
  }
  def createCriteriaRelation[A](clazz:Class[A],primaryKey:String,params:(String,Any)*):CriteriaRelation[A]={
    val queryBuilder = getService[EntityManager].getCriteriaBuilder
    val q = queryBuilder.createQuery(clazz)
    val relation = new CriteriaRelation[A](clazz,primaryKey,q) {
      override private[services] val builder: CriteriaBuilder = queryBuilder
    }
    params.foreach(relation.eq _ tupled)

    relation
  }
}

/**
 * ActiveRecord trait
 */
trait ActiveRecord {
  @Transient
  def save():this.type = {
    ActiveRecord.save(this)
  }
  @Transient
  def delete(): Unit ={
    ActiveRecord.delete(this)
  }
}
abstract class CriteriaRelation[A](val entityClass:Class[A],val primaryKey:String,val query:CriteriaQuery[A]) extends  Dynamic with QuerySupport[A]{
  private[services] val builder:CriteriaBuilder;
  private val currentRoot = query.from(entityClass)
  def eq(field:String,value:Any): Unit ={
    val expr: Predicate = builder.equal(currentRoot.get(field),value)
    query.where(Array(expr):_*)
  }
  override def order(params: (String, Any)*): CriteriaRelation.this.type = {
    params.foreach{
      case (field,value)=>
        String.valueOf(value).toLowerCase match{
          case "asc"=>
            query.orderBy(builder.asc(currentRoot.get(field)))
          case "desc"=>
            query.orderBy(builder.desc(currentRoot.get(field)))
        }
    }

    this
  }
}
trait QuerySupport[A]{
  protected val primaryKey:String
  private[orm] var limit:Int = -1
  private[orm] var offset:Int = -1

  private var underlying_result:Stream[A] = _
  protected def executeQuery: Stream[A] = {
    if(underlying_result == null)
      underlying_result = ActiveRecord.find(this)
    underlying_result
  }
  def order(params:(String,Any)*):this.type
  def asc(fields:String*):this.type={
    order(fields.map((_,"asc")):_*)
  }
  def desc(fields:String*):this.type={
    order(fields.map((_,"desc")):_*)
  }
  def exists():Boolean= limit(1).headOption.isDefined
  def limit(n:Int):this.type={
    limit=n
    this
  }
  def take:A= take(1).head
  def takeOption:Option[A]= take(1).headOption
  def take(n:Int):this.type={
    limit(n)
  }
  def first:A= first(1).head
  def firstOption:Option[A]= first(1).headOption
  def first(n:Int):this.type= {
    take(n).order(primaryKey-> "ASC")
  }
  def last:A= last(1).head
  def last(n:Int):this.type= {
    take(n).order(primaryKey->"DESC")
  }
  def offset(n:Int): this.type={
    offset = n
    this
  }
  @inline final def size = executeQuery.size
  @inline final def foreach[U](f: A => U) = executeQuery.foreach(f)
  @inline final def filter[U](f: A => Boolean) = executeQuery.filter(f)
  @inline final def head = executeQuery.head
  @inline final def headOption = executeQuery.headOption
  @inline final def tail = executeQuery.tail
  @inline final def map[B, That](f: A => B)(implicit bf: CanBuildFrom[Stream[A], B, That]): That =  executeQuery.map(f)
  @inline final def flatMap[B, That](f: A => GenTraversableOnce[B])(implicit bf: CanBuildFrom[Stream[A], B, That]): That = executeQuery.flatMap(f)
}

/**
 * query case class
 * @param entityClazz entity class
 * @param primaryKey primary key
 * @tparam A type parameter
 */
class Relation[A](val entityClazz:Class[A],val primaryKey:String) extends Dynamic with QuerySupport[A]{
  def this(entityClazz:Class[A],primaryKey:String,query:String,queryParams:Seq[Any]){
    this(entityClazz,primaryKey)
    if(query != null)
      this.queryClause = Some(query)

    this.queryParams = queryParams
  }
  private[orm] var orderBy:Option[String]=None

  private[orm] var queryClause:Option[String] = None
  private[orm] var queryParams:Seq[Any] = Nil

  private[orm] var updateQl:Option[String] = None
  private[orm] var updateParams:Seq[Any] = Nil


  /**
   * update method
   */
  def applyDynamicNamed(name:String)(params:(String,Any)*):Int=macro HallOrmMacroDefine.dslDynamicImplNamed[A,Int]

  def order(params:(String,Any)*):this.type= {
    params.foreach{case (key,value)=>
      orderBy match{
        case Some(o) =>
          orderBy = Some(o+",%s %s".format(key,value))
        case None=>
          orderBy = Some("%s %s".format(key,value))
      }
    }
    this
  }
  def internalUpdate(params:(String,Any)*): Int={
    var ql = ""
    var index = queryParams.size + 1
    val updateParams = mutable.Buffer[Any]()
    params.foreach{
      case (key,value)=>
        ql += "%s=?%s,".format(key,index)
        index += 1
        updateParams += value
    }

    this.updateParams = updateParams.toSeq
    if(ql.length>0){
      this.updateQl = Some(ql.dropRight(1))
    }

    ActiveRecord.updateRelation(this)
  }
  def delete():Int = {
    ActiveRecord.deleteRelation(this)
  }
}
abstract class ActiveRecordInstance[A](implicit val clazzTag:ClassTag[A]) extends Dynamic{
  /**
   * get model class and primary key
   */

  val clazz = clazzTag.runtimeClass.asInstanceOf[Class[A]]
  protected val field = clazz.getDeclaredFields.find(_.isAnnotationPresent(classOf[Id]))
  val primaryKey = field.getOrElse(throw new IllegalStateException("primary key is null")).getName

  /**
   * where method
   * sucha as:
   *
   * where(name="jcai",code="1232")
   * @param name method name
   * @param params method parameter
   * @return relation query object
   */
  def applyDynamicNamed(name:String)(params:(String,Any)*):CriteriaRelation[A]=macro HallOrmMacroDefine.findDynamicImplNamed[A,CriteriaRelation[A]]

  /**
   * find_by_xx_and_yy method
   * where(ql,parameters)
   * such as:
   *
   * ModelA.find_by_name_and_code("jcai","1232")
   * ModelA.where("name=?1 and code=?2","jcai","1232")
   *
   * @param name method name
   * @param params parameter list
   * @return Relation query instance
   */
  def applyDynamic(name:String)(params:Any*):Relation[A]= macro HallOrmMacroDefine.findDynamicImpl[A,Relation[A]]

  /**
   * retrieving single object
   * @param key primary key
   * @return entity object
   */
  def find(key:Any):A={
    internalFind(key).head
  }

  /**
   * find some records by primary key
   * @param keys key array
   * @return query object
   */
  def find(keys:Array[Any]):Relation[A]={
    internalFind(keys)
  }
  private def internalFind(key:Any):Relation[A]={
    key match{
      case _:Int|_:String|_:Long=>
        new Relation[A](clazz,primaryKey,"%s=?1".format(primaryKey),Seq(key))
      case keys:Array[_] =>
        new Relation[A](clazz,primaryKey,"%s IN (?1)".format(primaryKey),Seq(key))
      case None =>
        new Relation[A](clazz,primaryKey,null,Seq())
    }
  }
  //retrieving single object
  def take:A= take(1).head
  def take(n:Int):Relation[A]={
    internalFind(None).limit(n)
  }
  def first:A= first(1).head
  def first(n:Int=1):Relation[A]= {
    take(n).order(primaryKey-> "ASC")
  }
  def last:A= last(1).head
  def last(n:Int):Relation[A]= {
    take(n).order(primaryKey->"DESC")
  }
  def all:Relation[A]= {
    internalFind(None)
  }
  def asc(fields:String*):Relation[A]={
    internalFind(None).asc(fields:_*)
  }
  def desc(fields:String*):Relation[A]={
    internalFind(None).desc(fields:_*)
  }

  /*
  def find_each(start:Int=0,batchSize:Int = 100)(f:A=>Unit): Unit ={

  }
  */
}
