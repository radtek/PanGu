package nirvana.hall.v70.jpa

/**
  * Created by Administrator on 2017/5/31.
  */

import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * GafisLogicDbCase generated by hall orm
  */
object GafisLogicDbCaseBak extends ActiveRecordInstance[GafisLogicDbCaseBak]

@Entity
@Table(name = "GAFIS_LOGIC_DB_CASE_BAK"
)
class GafisLogicDbCaseBak extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "LOGIC_DB_PKID", length = 32)
  var logicDbPkid: java.lang.String = _
  @Column(name = "CASE_PKID", length = 32)
  var casePkid: java.lang.String = _


  def this(pkId: java.lang.String) {
    this()
    this.pkId = pkId
  }

  def this(pkId: java.lang.String, logicDbPkid: java.lang.String, casePkid: java.lang.String) {
    this()
    this.pkId = pkId
    this.logicDbPkid = logicDbPkid
    this.casePkid = casePkid
  }

  def this(gafisLogicDbCase: GafisLogicDbCase) {
    this()
    this.pkId = gafisLogicDbCase.pkId
    this.logicDbPkid = gafisLogicDbCase.logicDbPkid
    this.casePkid = gafisLogicDbCase.casePkid
  }
}