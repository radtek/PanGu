// Copyright 2012,2013,2015 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package nirvana.hall.support.services

import java.sql._
import javax.sql.DataSource

import monad.support.services.MonadException
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
 * jdbc操作数据库的通用类
 */
object JdbcDatabase {
  private val logger = LoggerFactory getLogger getClass

  def use[T](autoCommit: Boolean = true)(action: Connection => T)(implicit ds: DataSource): T = {
    val conn = getConnection(ds)
    try {
      conn.setAutoCommit(autoCommit)
      val ret = action(conn)
      if (!autoCommit) conn.commit()
      ret
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        if (!autoCommit) conn.rollback()
        throw MonadException.wrap(e)
    }finally {
      closeJdbc(conn)
    }
  }

  def update(sql: String)(psSetter:PreparedStatement=>Unit)(implicit ds: DataSource): Int = {
    use(autoCommit = true) { conn =>
      val st = conn.prepareStatement(sql)
      try {
        psSetter.apply(st)
        st.executeUpdate()
      } finally {
        closeJdbc(st)
      }
    }
  }

  def closeJdbc(resource: Any) {
    if (resource == null) return
    try {
      resource match {
        case c: Connection =>
          c.close()
        case s: Statement =>
          s.close()
        case r: ResultSet =>
          r.close()
        case _ => // do nothing
      }
    } catch {
      case NonFatal(e) => logger.error(e.getMessage, e)
    }
  }

  def queryFirst[T](sql: String)(psSetter: PreparedStatement => Unit)(mapper: ResultSet => T)(implicit ds: DataSource):Option[T]= {
    use(autoCommit = false) { conn =>
      val st = conn.prepareStatement(sql)
      try {
        psSetter.apply(st)
        val rs = st.executeQuery
        try {
          if(rs.next) Some(mapper(rs)) else None
        } finally {
          closeJdbc(rs)
        }
      } finally {
        closeJdbc(st)
      }
    }
  }

  /**
   * 查询自己控制是否遍历，通过手动执行rs.next遍历结果
   * @param sql
   * @param psSetter
   * @param mapper
   * @param ds
   * @return
   */
  def queryWithPsSetter2(sql: String)(psSetter: PreparedStatement => Unit)(mapper: ResultSet => Unit)(implicit ds: DataSource) {
    use(autoCommit = false) { conn =>
      val st = conn.prepareStatement(sql)
      try {
        psSetter.apply(st)
        val rs = st.executeQuery
        try {
          mapper(rs)
        } finally {
          closeJdbc(rs)
        }
      } finally {
        closeJdbc(st)
      }
    }
  }

  /**
   * 查询遍历所有的结果集,不用执行rs.next
   * @param sql
   * @param psSetter
   * @param mapper
   * @param ds
   * @return
   */
  def queryWithPsSetter(sql: String)(psSetter: PreparedStatement => Unit)(mapper: ResultSet => Unit)(implicit ds: DataSource) {
    use(autoCommit = false) { conn =>
      val st = conn.prepareStatement(sql)
      try {
        psSetter.apply(st)
        val rs = st.executeQuery
        try {
          while (rs.next) mapper(rs)
        } finally {
          closeJdbc(rs)
        }
      } finally {
        closeJdbc(st)
      }
    }
  }
  def getConnection(ds: DataSource) = ds.getConnection
}

