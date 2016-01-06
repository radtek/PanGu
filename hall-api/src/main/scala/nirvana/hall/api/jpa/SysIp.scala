package nirvana.hall.api.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table, Temporal, TemporalType}

import nirvana.hall.orm.services.{ActiveRecord, ActiveRecordInstance};

/**
 * SysIp generated by hall orm 
 */
object SysIp extends ActiveRecordInstance[SysIp]

@Entity
@Table(name = "SYS_IP"
)
class SysIp extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "ADDRE_IP", nullable = false, length = 50)
  var addreIp: java.lang.String = _
  @Column(name = "DELETE_FLAG")
  var deleteFlag: java.lang.Long = _
  @Column(name = "CREATE_USERNAME", length = 50)
  var createUsername: java.lang.String = _
  @Column(name = "CREATE_USERID", length = 50)
  var createUserid: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATE_DATETIME", length = 23)
  var createDatetime: java.util.Date = _
  @Column(name = "REMARK", length = 200)
  var remark: java.lang.String = _


  def this(pkId: java.lang.String, addreIp: java.lang.String) {
    this()
    this.pkId = pkId
    this.addreIp = addreIp
  }

  def this(pkId: java.lang.String, addreIp: java.lang.String, deleteFlag: java.lang.Long, createUsername: java.lang.String, createUserid: java.lang.String, createDatetime: java.util.Date, remark: java.lang.String) {
    this()
    this.pkId = pkId
    this.addreIp = addreIp
    this.deleteFlag = deleteFlag
    this.createUsername = createUsername
    this.createUserid = createUserid
    this.createDatetime = createDatetime
    this.remark = remark
  }


}


