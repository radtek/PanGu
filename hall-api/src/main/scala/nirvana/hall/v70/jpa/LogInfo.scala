package nirvana.hall.v70.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table, Temporal, TemporalType}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * LogInfo generated by hall orm 
 */
object LogInfo extends ActiveRecordInstance[LogInfo]

@Entity
@Table(name = "LOG_INFO"
)
class LogInfo extends ActiveRecord {


  @Id
  @Column(name = "NUM_ID", unique = true, nullable = false)
  var numId: Long = _
  @Column(name = "USER_ID", length = 32)
  var userId: java.lang.String = _
  @Column(name = "ORGANIZATION", length = 100)
  var organization: java.lang.String = _
  @Column(name = "USER_NAME", length = 100)
  var userName: java.lang.String = _
  @Column(name = "TERMINAL_ID", length = 100)
  var terminalId: java.lang.String = _
  @Column(name = "OPERATE_TYPE")
  var operateType: java.lang.Short = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "OPERATE_TIME", length = 23)
  var operateTime: java.util.Date = _
  @Column(name = "OPERATE_CONDITION", length = 1000)
  var operateCondition: java.lang.String = _
  @Column(name = "OPERATE_RESULT", length = 1)
  var operateResult: java.lang.String = _
  @Column(name = "OPERATE_OBJECT", length = 100)
  var operateObject: java.lang.String = _
  @Column(name = "DEPARTNAME", length = 100)
  var departname: java.lang.String = _
  @Column(name = "UPDATE_BEFORE", length = 100)
  var updateBefore: java.lang.String = _
  @Column(name = "UPDATE_AFTER", length = 100)
  var updateAfter: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "TIME", length = 23)
  var time: java.util.Date = _


  def this(numId: Long) {
    this()
    this.numId = numId
  }

  def this(numId: Long, userId: java.lang.String, organization: java.lang.String, userName: java.lang.String, terminalId: java.lang.String, operateType: java.lang.Short, operateTime: java.util.Date, operateCondition: java.lang.String, operateResult: java.lang.String, operateObject: java.lang.String, departname: java.lang.String, updateBefore: java.lang.String, updateAfter: java.lang.String, time: java.util.Date) {
    this()
    this.numId = numId
    this.userId = userId
    this.organization = organization
    this.userName = userName
    this.terminalId = terminalId
    this.operateType = operateType
    this.operateTime = operateTime
    this.operateCondition = operateCondition
    this.operateResult = operateResult
    this.operateObject = operateObject
    this.departname = departname
    this.updateBefore = updateBefore
    this.updateAfter = updateAfter
    this.time = time
  }


}


