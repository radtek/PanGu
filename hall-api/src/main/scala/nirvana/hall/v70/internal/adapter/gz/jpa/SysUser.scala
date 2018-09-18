package nirvana.hall.v70.internal.adapter.gz.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence._

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * SysUser generated by hall orm 
 */
object SysUser extends ActiveRecordInstance[SysUser]

@Entity
@Table(name = "SYS_USER"
)
class SysUser extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "LOGIN_NAME", nullable = false, length = 60)
  var loginName: java.lang.String = _
  @Column(name = "PASSWORD", length = 32)
  var password: java.lang.String = _
  @Column(name = "EMAIL", length = 60)
  var email: java.lang.String = _
  @Column(name = "TRUE_NAME", length = 60)
  var trueName: java.lang.String = _
  @Column(name = "DELETE_FLAG", length = 1)
  var deleteFlag: java.lang.String = _
  @Column(name = "REMARK", length = 90)
  var remark: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATE_DATETIME", length = 23)
  var createDatetime: java.util.Date = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "UPDATE_DATETIME", length = 23)
  var updateDatetime: java.util.Date = _
  @Column(name = "CREATE_USER_ID", length = 32)
  var createUserId: java.lang.String = _
  @Column(name = "UPDATE_USER_ID", length = 32)
  var updateUserId: java.lang.String = _
  @Column(name = "DEPART_CODE", length = 12)
  var departCode: java.lang.String = _
  @Column(name = "IDCARD", length = 18)
  var idcard: java.lang.String = _
  @Column(name = "POLICE_NUMBER", length = 10)
  var policeNumber: java.lang.String = _
  @Column(name = "GENDER_CODE", length = 1)
  var genderCode: java.lang.String = _
  @Column(name = "USER_TYPE", length = 1)
  var userType: java.lang.String = _
  @Column(name = "PHONE", length = 20)
  var phone: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "DEPART_START_DATE", length = 23)
  var departStartDate: java.util.Date = _
  @Column(name = "USER_STATUS", length = 1)
  var userStatus: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LAST_LOGIN_DATE", length = 23)
  var lastLoginDate: java.util.Date = _
//  @Column(name = "LOGIN_TIME_CONTROL", length = 1)
//  var loginTimeControl: java.lang.String = _


  def this(pkId: java.lang.String, loginName: java.lang.String) {
    this()
    this.pkId = pkId
    this.loginName = loginName
  }

  def this(pkId: java.lang.String, loginName: java.lang.String, password: java.lang.String, email: java.lang.String, trueName: java.lang.String, deleteFlag: java.lang.String, remark: java.lang.String, createDatetime: java.util.Date, updateDatetime: java.util.Date, createUserId: java.lang.String, updateUserId: java.lang.String, departCode: java.lang.String, idcard: java.lang.String, policeNumber: java.lang.String, genderCode: java.lang.String, userType: java.lang.String, phone: java.lang.String, departStartDate: java.util.Date, userStatus: java.lang.String, lastLoginDate: java.util.Date, loginTimeControl: java.lang.String) {
    this()
    this.pkId = pkId
    this.loginName = loginName
    this.password = password
    this.email = email
    this.trueName = trueName
    this.deleteFlag = deleteFlag
    this.remark = remark
    this.createDatetime = createDatetime
    this.updateDatetime = updateDatetime
    this.createUserId = createUserId
    this.updateUserId = updateUserId
    this.departCode = departCode
    this.idcard = idcard
    this.policeNumber = policeNumber
    this.genderCode = genderCode
    this.userType = userType
    this.phone = phone
    this.departStartDate = departStartDate
    this.userStatus = userStatus
    this.lastLoginDate = lastLoginDate
//    this.loginTimeControl = loginTimeControl
  }


}


