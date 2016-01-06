package nirvana.hall.api.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table, Temporal, TemporalType}

import nirvana.hall.orm.services.{ActiveRecord, ActiveRecordInstance};

/**
 * GafisCasePalm generated by hall orm 
 */
object GafisCasePalm extends ActiveRecordInstance[GafisCasePalm]

@Entity
@Table(name = "GAFIS_CASE_PALM"
)
class GafisCasePalm extends ActiveRecord {


  @Id
  @Column(name = "PALM_ID", unique = true, nullable = false, length = 25)
  var palmId: java.lang.String = _
  @Column(name = "SEQ_NO", length = 2)
  var seqNo: java.lang.String = _
  @Column(name = "CASE_ID", length = 23)
  var caseId: java.lang.String = _
  @Column(name = "IS_CORPSE", length = 1)
  var isCorpse: java.lang.String = _
  @Column(name = "CORPSE_NO", length = 23)
  var corpseNo: java.lang.String = _
  @Column(name = "REMAIN_PLACE", length = 30)
  var remainPlace: java.lang.String = _
  @Column(name = "FGP", length = 10)
  var fgp: java.lang.String = _
  @Column(name = "PATTERN", length = 20)
  var pattern: java.lang.String = _
  @Column(name = "RIDGE_COLOR", length = 1)
  var ridgeColor: java.lang.String = _
  @Column(name = "THAN_STATUS", length = 2)
  var thanStatus: java.lang.String = _
  @Column(name = "IS_ASSIST", length = 1)
  var isAssist: java.lang.String = _
  @Column(name = "PALM_IMG")
  var palmImg: java.sql.Blob = _
  @Column(name = "PALM_CPR")
  var palmCpr: java.sql.Blob = _
  @Column(name = "LT_COUNT")
  var ltCount: java.lang.Long = _
  @Column(name = "LL_COUNT")
  var llCount: java.lang.Long = _
  @Column(name = "LT_COUNT_MOD_MNT")
  var ltCountModMnt: java.lang.Long = _
  @Column(name = "LL_COUNT_MOD_MNT")
  var llCountModMnt: java.lang.Long = _
  @Column(name = "EDIT_COUNT")
  var editCount: java.lang.Long = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LT_DATE", length = 23)
  var ltDate: java.util.Date = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LL_DATE", length = 23)
  var llDate: java.util.Date = _
  @Column(name = "LT_OPERATOR", length = 32)
  var ltOperator: java.lang.String = _
  @Column(name = "LL_OPERATOR", length = 32)
  var llOperator: java.lang.String = _
  @Column(name = "CREATOR_UNIT_CODE", length = 12)
  var creatorUnitCode: java.lang.String = _
  @Column(name = "UPDATOR_UNIT_CODE", length = 12)
  var updatorUnitCode: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "MODIFIEDTIME", length = 23)
  var modifiedtime: java.util.Date = _
  @Column(name = "MODIFIEDPSN", length = 32)
  var modifiedpsn: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "INPUTTIME", length = 23)
  var inputtime: java.util.Date = _
  @Column(name = "INPUTPSN", length = 32)
  var inputpsn: java.lang.String = _
  @Column(name = "DELETAG", length = 1)
  var deletag: java.lang.String = _
  @Column(name = "REMARK", length = 1000)
  var remark: java.lang.String = _
  @Column(name = "LT_STATUS", length = 1)
  var ltStatus: java.lang.String = _
  @Column(name = "LL_STATUS", length = 1)
  var llStatus: java.lang.String = _
  @Column(name = "SID")
  var sid: java.lang.Long = _
  @Column(name = "MATCH_STATUS", length = 1)
  var matchStatus: java.lang.String = _
  @Column(name = "DEVELOP_METHOD", length = 2)
  var developMethod: java.lang.String = _
  @Column(name = "PALM_IMG_NOSQL_ID", length = 10)
  var palmImgNosqlId: java.lang.String = _
  @Column(name = "PALM_CPR_NOSQL_ID", length = 10)
  var palmCprNosqlId: java.lang.String = _
  @Column(name = "SEQ")
  var seq: java.lang.Long = _


  def this(palmId: java.lang.String) {
    this()
    this.palmId = palmId
  }

  def this(palmId: java.lang.String, seqNo: java.lang.String, caseId: java.lang.String, isCorpse: java.lang.String, corpseNo: java.lang.String, remainPlace: java.lang.String, fgp: java.lang.String, pattern: java.lang.String, ridgeColor: java.lang.String, thanStatus: java.lang.String, isAssist: java.lang.String, palmImg: java.sql.Blob, palmCpr: java.sql.Blob, ltCount: java.lang.Long, llCount: java.lang.Long, ltCountModMnt: java.lang.Long, llCountModMnt: java.lang.Long, editCount: java.lang.Long, ltDate: java.util.Date, llDate: java.util.Date, ltOperator: java.lang.String, llOperator: java.lang.String, creatorUnitCode: java.lang.String, updatorUnitCode: java.lang.String, modifiedtime: java.util.Date, modifiedpsn: java.lang.String, inputtime: java.util.Date, inputpsn: java.lang.String, deletag: java.lang.String, remark: java.lang.String, ltStatus: java.lang.String, llStatus: java.lang.String, sid: java.lang.Long, matchStatus: java.lang.String, developMethod: java.lang.String, palmImgNosqlId: java.lang.String, palmCprNosqlId: java.lang.String, seq: java.lang.Long) {
    this()
    this.palmId = palmId
    this.seqNo = seqNo
    this.caseId = caseId
    this.isCorpse = isCorpse
    this.corpseNo = corpseNo
    this.remainPlace = remainPlace
    this.fgp = fgp
    this.pattern = pattern
    this.ridgeColor = ridgeColor
    this.thanStatus = thanStatus
    this.isAssist = isAssist
    this.palmImg = palmImg
    this.palmCpr = palmCpr
    this.ltCount = ltCount
    this.llCount = llCount
    this.ltCountModMnt = ltCountModMnt
    this.llCountModMnt = llCountModMnt
    this.editCount = editCount
    this.ltDate = ltDate
    this.llDate = llDate
    this.ltOperator = ltOperator
    this.llOperator = llOperator
    this.creatorUnitCode = creatorUnitCode
    this.updatorUnitCode = updatorUnitCode
    this.modifiedtime = modifiedtime
    this.modifiedpsn = modifiedpsn
    this.inputtime = inputtime
    this.inputpsn = inputpsn
    this.deletag = deletag
    this.remark = remark
    this.ltStatus = ltStatus
    this.llStatus = llStatus
    this.sid = sid
    this.matchStatus = matchStatus
    this.developMethod = developMethod
    this.palmImgNosqlId = palmImgNosqlId
    this.palmCprNosqlId = palmCprNosqlId
    this.seq = seq
  }


}


