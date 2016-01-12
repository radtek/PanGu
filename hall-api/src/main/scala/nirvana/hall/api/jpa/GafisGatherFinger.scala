package nirvana.hall.api.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence._

import nirvana.hall.orm.services.{ActiveRecord, ActiveRecordInstance};

/**
 * GafisGatherFinger generated by hall orm 
 */
object GafisGatherFinger extends ActiveRecordInstance[GafisGatherFinger]

@Entity
@Table(name = "GAFIS_GATHER_FINGER"
)
class GafisGatherFinger extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "PERSON_ID", length = 32)
  var personId: java.lang.String = _
  @Column(name = "FGP", nullable = false)
  var fgp: Short = _
  @Column(name = "GROUP_ID")
  var groupId: java.lang.Short = _
  @Column(name = "LOBTYPE", nullable = false)
  var lobtype: Short = _
  @Column(name = "INPUTPSN", length = 32)
  var inputpsn: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "INPUTTIME", nullable = false, length = 23)
  var inputtime: java.util.Date = _
  @Column(name = "MODIFIEDPSN", length = 32)
  var modifiedpsn: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "MODIFIEDTIME", length = 23)
  var modifiedtime: java.util.Date = _
  @Column(name = "FGP_CASE", length = 1)
  var fgpCase: java.lang.String = _
  @Column(name = "AUDIT_STATUS")
  var auditStatus: java.lang.Short = _
  @Column(name = "DESCRIPTION", length = 100)
  var description: java.lang.String = _
  @Column(name = "MAIN_PATTERN")
  var mainPattern: java.lang.Short = _
  @Column(name = "VICE_PATTERN")
  var vicePattern: java.lang.Short = _
  @Lob
  @Column(name = "GATHER_DATA", nullable = false)
  var gatherData: Array[Byte] = _
  @Column(name = "SEQ")
  var seq: java.lang.Long = _
  @Column(name = "FINGER_DATA_NOSQL_ID", length = 10)
  var fingerDataNosqlId: java.lang.String = _
  @Column(name = "PARTITION_CITYCODE", length = 8)
  var partitionCitycode: java.lang.String = _


  def this(pkId: java.lang.String, fgp: Short, lobtype: Short, inputtime: java.util.Date, gatherData: Array[Byte]) {
    this()
    this.pkId = pkId
    this.fgp = fgp
    this.lobtype = lobtype
    this.inputtime = inputtime
    this.gatherData = gatherData
  }

  def this(pkId: java.lang.String, personId: java.lang.String, fgp: Short, groupId: java.lang.Short, lobtype: Short, inputpsn: java.lang.String, inputtime: java.util.Date, modifiedpsn: java.lang.String, modifiedtime: java.util.Date, fgpCase: java.lang.String, auditStatus: java.lang.Short, description: java.lang.String, mainPattern: java.lang.Short, vicePattern: java.lang.Short, gatherData: Array[Byte], seq: java.lang.Long, fingerDataNosqlId: java.lang.String, partitionCitycode: java.lang.String) {
    this()
    this.pkId = pkId
    this.personId = personId
    this.fgp = fgp
    this.groupId = groupId
    this.lobtype = lobtype
    this.inputpsn = inputpsn
    this.inputtime = inputtime
    this.modifiedpsn = modifiedpsn
    this.modifiedtime = modifiedtime
    this.fgpCase = fgpCase
    this.auditStatus = auditStatus
    this.description = description
    this.mainPattern = mainPattern
    this.vicePattern = vicePattern
    this.gatherData = gatherData
    this.seq = seq
    this.fingerDataNosqlId = fingerDataNosqlId
    this.partitionCitycode = partitionCitycode
  }


}


