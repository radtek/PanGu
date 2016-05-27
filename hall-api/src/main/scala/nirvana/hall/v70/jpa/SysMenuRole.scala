package nirvana.hall.v70.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table, Temporal, TemporalType}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * SysMenuRole generated by hall orm 
 */
object SysMenuRole extends ActiveRecordInstance[SysMenuRole]

@Entity
@Table(name = "SYS_MENU_ROLE"
)
class SysMenuRole extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "ROLE_ID", nullable = false, length = 32)
  var roleId: java.lang.String = _
  @Column(name = "MENU_CODE", length = 10)
  var menuCode: java.lang.String = _
  @Column(name = "REMARK", length = 300)
  var remark: java.lang.String = _
  @Column(name = "CREATE_USER", nullable = false, length = 60)
  var createUser: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATE_DATETIME", length = 23)
  var createDatetime: java.util.Date = _
  @Column(name = "UPDATE_USER", length = 60)
  var updateUser: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "UPDATE_DATETIME", length = 23)
  var updateDatetime: java.util.Date = _
  @Column(name = "CREATE_USER_ID", length = 32)
  var createUserId: java.lang.String = _
  @Column(name = "UPDATE_USER_ID", length = 32)
  var updateUserId: java.lang.String = _
  @Column(name = "ONLY_READ")
  var onlyRead: java.lang.Long = _


  def this(pkId: java.lang.String, roleId: java.lang.String, createUser: java.lang.String) {
    this()
    this.pkId = pkId
    this.roleId = roleId
    this.createUser = createUser
  }

  def this(pkId: java.lang.String, roleId: java.lang.String, menuCode: java.lang.String, remark: java.lang.String, createUser: java.lang.String, createDatetime: java.util.Date, updateUser: java.lang.String, updateDatetime: java.util.Date, createUserId: java.lang.String, updateUserId: java.lang.String, onlyRead: java.lang.Long) {
    this()
    this.pkId = pkId
    this.roleId = roleId
    this.menuCode = menuCode
    this.remark = remark
    this.createUser = createUser
    this.createDatetime = createDatetime
    this.updateUser = updateUser
    this.updateDatetime = updateDatetime
    this.createUserId = createUserId
    this.updateUserId = updateUserId
    this.onlyRead = onlyRead
  }


}


