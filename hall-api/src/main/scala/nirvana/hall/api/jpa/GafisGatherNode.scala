package nirvana.hall.api.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table, Temporal, TemporalType}

import nirvana.hall.orm.services.{ActiveRecord, ActiveRecordInstance};

/**
 * GafisGatherNode generated by hall orm 
 */
object GafisGatherNode extends ActiveRecordInstance[GafisGatherNode]

@Entity
@Table(name = "GAFIS_GATHER_NODE"
)
class GafisGatherNode extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "NODE_CODE", length = 20)
  var nodeCode: java.lang.String = _
  @Column(name = "NODE_NAME", length = 50)
  var nodeName: java.lang.String = _
  @Column(name = "NODE_REQUEST", nullable = false, length = 200)
  var nodeRequest: java.lang.String = _
  @Column(name = "DELETE_FLAG")
  var deleteFlag: java.lang.Long = _
  @Column(name = "CREATE_USER_ID", nullable = false, length = 50)
  var createUserId: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATE_DATETIME", nullable = false, length = 23)
  var createDatetime: java.util.Date = _
  @Column(name = "UPDATE_USER_ID", length = 50)
  var updateUserId: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "UPDATE_DATETIME", length = 23)
  var updateDatetime: java.util.Date = _
  @Column(name = "NODE_IMG", length = 20)
  var nodeImg: java.lang.String = _


  def this(pkId: java.lang.String, nodeRequest: java.lang.String, createUserId: java.lang.String, createDatetime: java.util.Date) {
    this()
    this.pkId = pkId
    this.nodeRequest = nodeRequest
    this.createUserId = createUserId
    this.createDatetime = createDatetime
  }

  def this(pkId: java.lang.String, nodeCode: java.lang.String, nodeName: java.lang.String, nodeRequest: java.lang.String, deleteFlag: java.lang.Long, createUserId: java.lang.String, createDatetime: java.util.Date, updateUserId: java.lang.String, updateDatetime: java.util.Date, nodeImg: java.lang.String) {
    this()
    this.pkId = pkId
    this.nodeCode = nodeCode
    this.nodeName = nodeName
    this.nodeRequest = nodeRequest
    this.deleteFlag = deleteFlag
    this.createUserId = createUserId
    this.createDatetime = createDatetime
    this.updateUserId = updateUserId
    this.updateDatetime = updateDatetime
    this.nodeImg = nodeImg
  }


}


