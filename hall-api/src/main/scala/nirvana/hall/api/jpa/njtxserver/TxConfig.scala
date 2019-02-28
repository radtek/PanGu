package nirvana.hall.api.jpa.njtxserver

// Generated Feb 27, 2019 5:23:16 PM by Stark Activerecord generator 4.3.1.Final


import javax.persistence._

import stark.activerecord.services.ActiveRecord
import stark.activerecord.services.ActiveRecordInstance;

/**
 * TxConfig generated by stark activerecord generator
 */
object TxConfig extends ActiveRecordInstance[TxConfig]

@Entity
@Table(name="TX_CONFIG")
class TxConfig extends ActiveRecord {

  @Id
  @Column(name="id", nullable=false, length=32)
  var id:java.lang.String =  _
  @Column(name="station_name", length=1000)
  var stationName:java.lang.String =  _
  @Column(name="station_code", length=12)
  var stationCode:java.lang.String =  _
  @Column(name="start_time", length=10)
  var startTime:java.util.Date =  _
  @Column(name="flag")
  var flag:java.lang.Integer =  _
  @Column(name="ip", length=50)
  var ip:java.lang.String =  _
  @Column(name="port", length=10)
  var port:java.lang.String =  _



  def this(id:java.lang.String) {
    this()
    this.id = id
  }
  def this(id:java.lang.String, stationName:java.lang.String, stationCode:java.lang.String, startTime:java.util.Date, flag:java.lang.Integer, ip:java.lang.String, port:java.lang.String) {
    this()
    this.id = id
    this.stationName = stationName
    this.stationCode = stationCode
    this.startTime = startTime
    this.flag = flag
    this.ip = ip
    this.port = port
  }

   




}


