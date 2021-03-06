package nirvana.hall.webservice.internal.greathand.jpa

import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}


object SyncCronLog extends ActiveRecordInstance[SyncCronLog]

@Entity
@Table(name = "sync_cron_log")
class SyncCronLog extends ActiveRecord {
  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "keyid", length = 32)
  var keyId: java.lang.String = _
  @Column(name = "TYP", length = 32)
  var typ: java.lang.String = _
  @Column(name = "input_time")
  var inputTime: java.util.Date = _
  @Column(name = "flag")
  var flag: java.lang.String = _
  @Column(name = "errormsg")
  var errorMsg: java.lang.String = _

  def this(pkId: java.lang.String) {
    this()
    this.pkId = pkId
  }

  def this(pkId: java.lang.String
           , keyId: java.lang.String
           , typ: java.lang.String
           , inputTime: java.util.Date
           , flag: java.lang.String
           , errorMsg: java.lang.String) {
    this()
    this.pkId = pkId
    this.keyId = keyId
    this.typ = typ
    this.inputTime = inputTime
    this.flag = flag
    this.errorMsg = errorMsg
  }
}
