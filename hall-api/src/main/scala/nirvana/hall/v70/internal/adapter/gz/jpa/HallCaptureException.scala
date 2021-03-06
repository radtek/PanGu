package nirvana.hall.v70.internal.adapter.gz.jpa

import javax.persistence._

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * HallCaptureException generated by hall orm
  * Created by ssj on 2017/10/13.
  */
object HallCaptureException extends ActiveRecordInstance[HallCaptureException]

@Entity
@Table(name = "Hall_Capture_Exception")
class HallCaptureException extends ActiveRecord {

  @Id
  @Column(name = "UUID", unique = true, nullable = false, length = 32)
  var uuid: java.lang.String = _
  @Column(name = "P_UUID", length = 32)
  var puuid: java.lang.String = _
  @Lob
  @Column(name = "MSG")
  var msg: java.lang.String = _
  @Column(name = "ERR_TYPE", length = 1)
  var errtype: java.lang.String = _


  def this(uuid: java.lang.String) {
    this()
    this.uuid = uuid
  }

  def this(uuid: java.lang.String, puuid: java.lang.String, msg: java.lang.String, errtype: java.lang.String) {
    this()
    this.uuid = uuid
    this.puuid = puuid
    this.msg = msg
    this.errtype = errtype
  }

}
