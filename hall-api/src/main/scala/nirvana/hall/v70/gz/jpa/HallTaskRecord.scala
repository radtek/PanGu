package nirvana.hall.v70.gz.jpa

import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * HALLTASKRECORD generated by hall orm
  * Created by ssj on 2017/10/13.
  */
object HallTaskRecord extends ActiveRecordInstance[HallTaskRecord]

@Entity
@Table(name = "Hall_Task_Record")
class HallTaskRecord extends ActiveRecord {

  @Id
  @Column(name = "UUID", unique = true, nullable = false, length = 32)
  var uuid: java.lang.String = _
  @Column(name = "P_UUID", length = 32)
  var puuid: java.lang.String = _
  @Column(name = "PERSONID", length = 23)
  var personid: java.lang.String = _
  @Column(name = "QUERY_TYPE", length = 1)
  var querytype: java.lang.String = _
  @Column(name = "ORA_SID", length = 20)
  var orasid: java.lang.String = _
  @Column(name = "TASKSENDDATE", length = 23)
  var tasksenddate: java.util.Date = _
  @Column(name = "STATUS", length = 23)
  var status:  java.lang.String = _


  def this(uuid: java.lang.String) {
    this()
    this.uuid = uuid
  }

  def this(uuid: java.lang.String, puuid: java.lang.String, personid: java.lang.String, querytype: java.lang.String, orasid: java.lang.String, tasksenddate: java.util.Date, status: java.lang.String) {
    this()
    this.uuid = uuid
    this.puuid = puuid
    this.personid = personid
    this.querytype = querytype
    this.orasid = orasid
    this.tasksenddate = tasksenddate
    this.status = status
  }
}
