package nirvana.hall.v70.internal.adapter.nj.jpa

// Generated 2017-10-23 11:12:54 by Stark Activerecord generator 4.3.1.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import stark.activerecord.services.ActiveRecord;
import stark.activerecord.services.ActiveRecordInstance;

/**
  * GafisLogicDb generated by stark activerecord generator
  */
object GafisLogicDb extends ActiveRecordInstance[GafisLogicDb]

@Entity
@Table(name = "GAFIS_LOGIC_DB"
)
class GafisLogicDb extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "LOGIC_CODE", length = 32)
  var logicCode: java.lang.String = _
  @Column(name = "LOGIC_NAME", length = 32)
  var logicName: java.lang.String = _
  @Column(name = "LOGIC_CATEGORY", length = 1)
  var logicCategory: java.lang.String = _
  @Column(name = "LOGIC_DELTAG", length = 1)
  var logicDeltag: java.lang.String = _
  @Column(name = "LOGIC_REMARK", length = 60)
  var logicRemark: java.lang.String = _
  @Column(name = "LOGIC_ISDEFAULTTAG", length = 1)
  var logicIsdefaulttag: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "LOGIC_CREORUPD_DATE", length = 8)
  var logicCreorupdDate: java.util.Date = _
  @Column(name = "QUERYPARAMS")
  var queryparams: java.sql.Clob = _


  def this(pkId: java.lang.String) {
    this()
    this.pkId = pkId
  }

  def this(pkId: java.lang.String, logicCode: java.lang.String, logicName: java.lang.String, logicCategory: java.lang.String, logicDeltag: java.lang.String, logicRemark: java.lang.String, logicIsdefaulttag: java.lang.String, logicCreorupdDate: java.util.Date, queryparams: java.sql.Clob) {
    this()
    this.pkId = pkId
    this.logicCode = logicCode
    this.logicName = logicName
    this.logicCategory = logicCategory
    this.logicDeltag = logicDeltag
    this.logicRemark = logicRemark
    this.logicIsdefaulttag = logicIsdefaulttag
    this.logicCreorupdDate = logicCreorupdDate
    this.queryparams = queryparams
  }


}

