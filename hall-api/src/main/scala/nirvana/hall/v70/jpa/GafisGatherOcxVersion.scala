package nirvana.hall.v70.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table, Temporal, TemporalType}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * GafisGatherOcxVersion generated by hall orm 
 */
object GafisGatherOcxVersion extends ActiveRecordInstance[GafisGatherOcxVersion]

@Entity
@Table(name = "GAFIS_GATHER_OCX_VERSION"
)
class GafisGatherOcxVersion extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "VERSION", length = 10)
  var version: java.lang.String = _
  @Column(name = "NAME", length = 30)
  var name: java.lang.String = _
  @Column(name = "TYPE", length = 2)
  var ocxType: java.lang.String = _
  @Column(name = "REMARK", length = 300)
  var remark: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATE_DATETIME", length = 23)
  var createDatetime: java.util.Date = _
  @Column(name = "DELETAG", length = 1)
  var deletag: java.lang.String = _


  def this(pkId: java.lang.String) {
    this()
    this.pkId = pkId
  }

  def this(pkId: java.lang.String, name: java.lang.String, ocxType: java.lang.String, remark: java.lang.String, createDatetime: java.util.Date, deletag: java.lang.String)
  {
    this()
    this.pkId = pkId
    this.name = name
    this.ocxType = ocxType
    this.remark = remark
    this.createDatetime = createDatetime
    this.deletag = deletag
  }


}


