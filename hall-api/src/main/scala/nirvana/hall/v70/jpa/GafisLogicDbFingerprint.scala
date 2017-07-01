package nirvana.hall.v70.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * GafisLogicDbFingerprint generated by hall orm 
 */
object GafisLogicDbFingerprint extends ActiveRecordInstance[GafisLogicDbFingerprint]

@Entity
@Table(name = "GAFIS_LOGIC_DB_FINGERPRINT"
)
class GafisLogicDbFingerprint extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "LOGIC_DB_PKID", length = 32)
  var logicDbPkid: java.lang.String = _
  @Column(name = "FINGERPRINT_PKID", length = 32)
  var fingerprintPkid: java.lang.String = _


  def this(pkId: java.lang.String) {
    this()
    this.pkId = pkId
  }

  def this(pkId: java.lang.String, logicDbPkid: java.lang.String, fingerprintPkid: java.lang.String) {
    this()
    this.pkId = pkId
    this.logicDbPkid = logicDbPkid
    this.fingerprintPkid = fingerprintPkid
  }


}

