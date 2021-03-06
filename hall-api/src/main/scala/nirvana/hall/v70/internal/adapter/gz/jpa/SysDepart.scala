package nirvana.hall.v70.internal.adapter.gz.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * SysDepart generated by hall orm 
 */
object SysDepart extends ActiveRecordInstance[SysDepart]

@Entity
@Table(name = "SYS_DEPART"
)
class SysDepart extends ActiveRecord {


  @Id
  @Column(name = "CODE", unique = true, nullable = false, length = 12)
  var code: java.lang.String = _
  @Column(name = "NAME", length = 200)
  var name: java.lang.String = _
  @Column(name = "LEADER", length = 60)
  var leader: java.lang.String = _
  @Column(name = "REMARK", length = 90)
  var remark: java.lang.String = _
  @Column(name = "DELETE_FLAG", length = 1)
  var deleteFlag: java.lang.String = _
  @Column(name = "IS_LEAF", length = 1)
  var isLeaf: java.lang.String = _
  @Column(name = "PARENT_ID", length = 12)
  var parentId: java.lang.String = _
  @Column(name = "DEPT_LEVEL", length = 1)
  var deptLevel: java.lang.String = _
  @Column(name = "LONGITUDE")
  var longitude: java.lang.Integer = _
  @Column(name = "LATITUDE")
  var latitude: java.lang.Integer = _
  @Column(name = "PHONE", length = 30)
  var phone: java.lang.String = _
  @Column(name = "LONG_NAME", length = 200)
  var longName: java.lang.String = _
  @Column(name = "IS_HAVE_CHAMBER", length = 1)
  var isHaveChamber: java.lang.String = _
  @Column(name = "CHAMBER_TYPE")
  var chamberType: java.lang.Short = _
  @Column(name = "IS_SPECIAL")
  var isSpecial: java.lang.Short = _
  @Column(name = "INTEGRATION_TYPE", length = 2)
  var integrationType: java.lang.String = _


  def this(code: java.lang.String) {
    this()
    this.code = code
  }

  def this(code: java.lang.String, name: java.lang.String, leader: java.lang.String, remark: java.lang.String, deleteFlag: java.lang.String, isLeaf: java.lang.String, parentId: java.lang.String, deptLevel: java.lang.String, longitude: java.lang.Integer, latitude: java.lang.Integer, phone: java.lang.String, longName: java.lang.String, isHaveChamber: java.lang.String, chamberType: java.lang.Short, isSpecial: java.lang.Short, integrationType: java.lang.String) {
    this()
    this.code = code
    this.name = name
    this.leader = leader
    this.remark = remark
    this.deleteFlag = deleteFlag
    this.isLeaf = isLeaf
    this.parentId = parentId
    this.deptLevel = deptLevel
    this.longitude = longitude
    this.latitude = latitude
    this.phone = phone
    this.longName = longName
    this.isHaveChamber = isHaveChamber
    this.chamberType = chamberType
    this.isSpecial = isSpecial
    this.integrationType = integrationType
  }


}


