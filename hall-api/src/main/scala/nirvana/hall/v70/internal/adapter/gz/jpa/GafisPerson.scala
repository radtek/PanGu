package nirvana.hall.v70.internal.adapter.gz.jpa

// Generated 2017-5-26 13:25:18 by Stark Activerecord generator 4.3.1.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import stark.activerecord.services.ActiveRecord;
import stark.activerecord.services.ActiveRecordInstance;

/**
  * GafisPerson generated by stark activerecord generator
  */
object GafisPerson extends ActiveRecordInstance[GafisPerson]

@Entity
@Table(name = "GAFIS_PERSON")
class GafisPerson extends ActiveRecord {
  @Id
  @Column(name = "PERSONID", unique = true, nullable = false, length = 23)
  var personid: java.lang.String = _
  @Column(name = "IDCARDNO", length = 18)
  var idcardno: java.lang.String = _
  @Column(name = "NAME", length = 60)
  var name: java.lang.String = _
  @Column(name = "SPELLNAME", length = 60)
  var spellname: java.lang.String = _
  @Column(name = "USEDNAME", length = 60)
  var usedname: java.lang.String = _
  @Column(name = "USEDSPELL", length = 60)
  var usedspell: java.lang.String = _
  @Column(name = "ALIASNAME", length = 60)
  var aliasname: java.lang.String = _
  @Column(name = "ALIASSPELL", length = 60)
  var aliasspell: java.lang.String = _
  @Column(name = "SEX_CODE", length = 1)
  var sexCode: java.lang.String = _
  @Column(name = "NATIVEPLACE_CODE", length = 6)
  var nativeplaceCode: java.lang.String = _
  @Column(name = "NATION_CODE", length = 2)
  var nationCode: java.lang.String = _
  @Column(name = "IFMARRY_CODE", length = 2)
  var ifmarryCode: java.lang.String = _
  @Column(name = "TONE_CODE", length = 6)
  var toneCode: java.lang.String = _
  @Column(name = "TONE", length = 60)
  var tone: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "BIRTHDAYST", length = 8)
  var birthdayst: java.util.Date = _
  @Temporal(TemporalType.DATE)
  @Column(name = "BIRTHDAYED", length = 8)
  var birthdayed: java.util.Date = _
  @Column(name = "BIRTH_CODE", length = 6)
  var birthCode: java.lang.String = _
  @Column(name = "BIRTH_STREET", length = 60)
  var birthStreet: java.lang.String = _
  @Column(name = "BIRTHDETAIL", length = 90)
  var birthdetail: java.lang.String = _
  @Column(name = "DOOR", length = 6)
  var door: java.lang.String = _
  @Column(name = "DOOR_STREET", length = 60)
  var doorStreet: java.lang.String = _
  @Column(name = "DOORDETAIL", length = 90)
  var doordetail: java.lang.String = _
  @Column(name = "ADDRESS", length = 6)
  var address: java.lang.String = _
  @Column(name = "ADDRESS_STREET", length = 60)
  var addressStreet: java.lang.String = _
  @Column(name = "ADDRESSDETAIL", length = 90)
  var addressdetail: java.lang.String = _
  @Column(name = "CULTURE_CODE", length = 2)
  var cultureCode: java.lang.String = _
  @Column(name = "SOURCEINCOME_CODE", length = 2)
  var sourceincomeCode: java.lang.String = _
  @Column(name = "FAITH_CODE", length = 2)
  var faithCode: java.lang.String = _
  @Column(name = "HAVEEMPLOYMENT", length = 1)
  var haveemployment: java.lang.String = _
  @Column(name = "JOB_CODE", length = 4)
  var jobCode: java.lang.String = _
  @Column(name = "HEADSHIP", length = 60)
  var headship: java.lang.String = _
  @Column(name = "EMPLOYUNIT", length = 60)
  var employunit: java.lang.String = _
  @Column(name = "EMPLOYADDRESS", length = 90)
  var employaddress: java.lang.String = _
  @Column(name = "OTHERSPECIALTY", length = 300)
  var otherspecialty: java.lang.String = _
  @Column(name = "SPECIALIDENTITY_CODE", length = 4)
  var specialidentityCode: java.lang.String = _
  @Column(name = "POLITICS_CODE", length = 3)
  var politicsCode: java.lang.String = _
  @Column(name = "ISTRANSIENTPOP", length = 1)
  var istransientpop: java.lang.String = _
  @Column(name = "ISTEMPREGIST", length = 1)
  var istempregist: java.lang.String = _
  @Column(name = "HAVEPERMIT", length = 1)
  var havepermit: java.lang.String = _
  @Column(name = "HAVERESIDENCE", length = 1)
  var haveresidence: java.lang.String = _
  @Column(name = "ISSERVICE", length = 1)
  var isservice: java.lang.String = _
  @Column(name = "SPECIALGROUP_CODE", length = 1)
  var specialgroupCode: java.lang.String = _
  @Column(name = "HAVESEPARATION", length = 1)
  var haveseparation: java.lang.String = _
  @Column(name = "ISMIGRANTWORKER", length = 1)
  var ismigrantworker: java.lang.String = _
  @Column(name = "NAMEOFSCHOOL", length = 90)
  var nameofschool: java.lang.String = _
  @Column(name = "ISTRAINING", length = 1)
  var istraining: java.lang.String = _
  @Column(name = "HAVECERTIFICATE", length = 1)
  var havecertificate: java.lang.String = _
  @Column(name = "STATUREST", precision = 3, scale = 0)
  var staturest: java.lang.Short = _
  @Column(name = "AVOIRDUPOIS", precision = 4, scale = 1)
  var avoirdupois: java.math.BigDecimal = _
  @Column(name = "FOOTSIZE", precision = 3, scale = 1)
  var footsize: java.math.BigDecimal = _
  @Column(name = "SHOELENGTH", precision = 3, scale = 1)
  var shoelength: java.math.BigDecimal = _
  @Column(name = "BODILYFORM_CODE", length = 4)
  var bodilyformCode: java.lang.String = _
  @Column(name = "FACEFORM_CODE", length = 4)
  var faceformCode: java.lang.String = _
  @Column(name = "ISEYEGLASS", length = 1)
  var iseyeglass: java.lang.String = _
  @Column(name = "SHOESIZE", precision = 3, scale = 0)
  var shoesize: java.lang.Short = _
  @Column(name = "BLOODTYPE_CODE", length = 1)
  var bloodtypeCode: java.lang.String = _
  @Column(name = "GATHER_ORG_CODE", length = 12)
  var gatherOrgCode: java.lang.String = _
  @Column(name = "IPADDRESS", length = 15)
  var ipaddress: java.lang.String = _
  @Column(name = "GATHERER_ID", length = 32)
  var gathererId: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "GATHER_DATE", length = 14)
  var gatherDate: java.util.Date = _
  @Column(name = "GATHER_TYPE_ID", length = 32)
  var gatherTypeId: java.lang.String = _
  @Column(name = "STATUS", length = 2)
  var status: java.lang.String = _
  @Column(name = "ISFINGERREPEAT", length = 1)
  var isfingerrepeat: java.lang.String = _
  @Column(name = "FINGERREPEATNO", length = 32)
  var fingerrepeatno: java.lang.String = _
  @Column(name = "TASK_SOURCE", length = 2)
  var taskSource: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "RECEIVE_TIME", length = 14)
  var receiveTime: java.util.Date = _
  @Column(name = "ISRETURN", length = 1)
  var isreturn: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "RETURN_TIME", length = 14)
  var returnTime: java.util.Date = _
  @Column(name = "ANNEX", length = 1500)
  var annex: java.lang.String = _
  @Column(name = "INPUTPSN", length = 32)
  var inputpsn: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "INPUTTIME", length = 23)
  var inputtime: java.util.Date = _
  @Column(name = "MODIFIEDPSN", length = 32)
  var modifiedpsn: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "MODIFIEDTIME", length = 23)
  var modifiedtime: java.util.Date = _
  @Column(name = "DELETAG", length = 1)
  var deletag: java.lang.String = _
  @Column(name = "SCHEDULE", length = 10)
  var schedule: java.lang.String = _
  @Column(name = "APPROVAL", length = 1)
  var approval: java.lang.String = _
  @Column(name = "DNA_CODE", length = 30)
  var dnaCode: java.lang.String = _
  @Column(name = "GATHER_CATEGORY", length = 1)
  var gatherCategory: java.lang.String = _
  @Column(name = "PERSON_CATEGORY", length = 3)
  var personCategory: java.lang.String = _
  @Column(name = "AUDITOR", length = 32)
  var auditor: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "AUDITEDTIME", length = 8)
  var auditedtime: java.util.Date = _
  @Column(name = "ISREGATHER", length = 1)
  var isregather: java.lang.String = _
  @Column(name = "GATHER_FINGER_MODE", length = 1)
  var gatherFingerMode: java.lang.String = _
  @Column(name = "CASE_NAME", length = 100)
  var caseName: java.lang.String = _
  @Column(name = "CASE_CLASSES", length = 6)
  var caseClasses: java.lang.String = _
  @Column(name = "REASON", length = 6)
  var reason: java.lang.String = _
  @Column(name = "GATHER_FINGER_NUM", precision = 65535, scale = 32767)
  var gatherFingerNum: java.math.BigDecimal = _
  @Column(name = "FINGER_REMARK", length = 300)
  var fingerRemark: java.lang.String = _
  @Column(name = "DEPRTMAC", length = 100)
  var deprtmac: java.lang.String = _
  @Column(name = "GATHERDEPARTCODE", length = 50)
  var gatherdepartcode: java.lang.String = _
  @Column(name = "GATHERUSERID", length = 50)
  var gatheruserid: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "GATHER_FINGER_TIME", length = 23)
  var gatherFingerTime: java.util.Date = _
  @Column(name = "IS_SEND_TL", precision = 65535, scale = 32767)
  var isSendTl: java.math.BigDecimal = _
  @Column(name = "CASE_BRIEF_CONTENTS", length = 1500)
  var caseBriefContents: java.lang.String = _
  @Column(name = "PUSH_STATUS", length = 1)
  var pushStatus: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "PUSH_DATE", length = 14)
  var pushDate: java.util.Date = _
  @Column(name = "REMARK", length = 2000)
  var remark: java.lang.String = _
  @Column(name = "DATA_SOURCES", precision = 1, scale = 0)
  var dataSources: java.lang.String = _
  @Column(name = "FINGERSHOW_STATUS", precision = 1, scale = 0)
  var fingershowStatus: java.lang.Short = _
  @Column(name = "CITY_CODE", length = 4)
  var cityCode: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "DELAY_DEADLINE", length = 8)
  var delayDeadline: java.util.Date = _
  @Column(name = "FPT_GATHER_DEPART_CODE", length = 12)
  var fptGatherDepartCode: java.lang.String = _
  @Column(name = "FPT_GATHER_DEPART_NAME", length = 90)
  var fptGatherDepartName: java.lang.String = _
  @Column(name = "SID")
  var sid: java.lang.Long = _
  @Column(name = "BLOW_CODE", length = 6)
  var blowCode: java.lang.String = _
  @Column(name = "BLOW_STREET", length = 60)
  var blowStreet: java.lang.String = _
  @Column(name = "BLOW_DETAIL", length = 90)
  var blowDetail: java.lang.String = _
  @Column(name = "BLOW_LONGITUDE", length = 30)
  var blowLongitude: java.lang.String = _
  @Column(name = "BLOW_LATITUDE", length = 30)
  var blowLatitude: java.lang.String = _
  @Column(name = "BLOW_EASTWEST", length = 1)
  var blowEastwest: java.lang.Character = _
  @Column(name = "BLOW_NORTHSOUTH", length = 1)
  var blowNorthsouth: java.lang.Character = _
  @Column(name = "SEQ", precision = 65535, scale = 32767)
  var seq: java.math.BigDecimal = _
  @Column(name = "CARDID", length = 23)
  var cardid: java.lang.String = _
  @Column(name = "RECORDMARK", length = 1)
  var recordmark: java.lang.Character = _
  @Column(name = "RECORDSITUATION", length = 3072)
  var recordsituation: java.lang.String = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "VALID_DATE", length = 8)
  var validDate: java.util.Date = _
  @Temporal(TemporalType.DATE)
  @Column(name = "ARRIVE_LOCAL_DATE", length = 8)
  var arriveLocalDate: java.util.Date = _
  @Temporal(TemporalType.DATE)
  @Column(name = "LEAVE_LOCAL_DATE", length = 8)
  var leaveLocalDate: java.util.Date = _
  @Column(name = "DB_SOURCE", length = 30)
  var dbSource: java.lang.String = _
  @Column(name = "DB_SOURCE_DIS", length = 100)
  var dbSourceDis: java.lang.String = _
  @Column(name = "JOB_DES", length = 100)
  var jobDes: java.lang.String = _
  @Column(name = "IS_XJSSMZ", length = 1)
  var isXjssmz: java.lang.Character = _
  @Column(name = "PASSPORT_NUM", length = 30)
  var passportNum: java.lang.String = _
  @Column(name = "COUNTRY_CODE", length = 6)
  var countryCode: java.lang.String = _
  @Column(name = "FOREIGN_NAME", length = 100)
  var foreignName: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "PASSPORT_VALID_DATE", length = 8)
  var passportValidDate: java.util.Date = _
  @Column(name = "VISA_PLACE", length = 100)
  var visaPlace: java.lang.String = _
  @Column(name = "PASSPORT_TYPE", length = 3)
  var passportType: java.lang.String = _
  @Temporal(TemporalType.DATE)
  @Column(name = "VISA_DATE", length = 8)
  var visaDate: java.util.Date = _
  @Column(name = "ASSIST_LEVEL", length = 1)
  var assistLevel: java.lang.String = _
  @Column(name = "ASSIST_BONUS", length = 6)
  var assistBonus: java.lang.String = _
  @Column(name = "ASSIST_PURPOSE", length = 5)
  var assistPurpose: java.lang.String = _
  @Column(name = "ASSIST_REF_PERSON", length = 23)
  var assistRefPerson: java.lang.String = _
  @Column(name = "ASSIST_REF_CASE", length = 23)
  var assistRefCase: java.lang.String = _
  @Column(name = "ASSIST_VALID_DATE", length = 1)
  var assistValidDate: java.lang.String = _
  @Column(name = "ASSIST_EXPLAIN", length = 512)
  var assistExplain: java.lang.String = _
  @Column(name = "ASSIST_DEPT_CODE", length = 12)
  var assistDeptCode: java.lang.String = _
  @Column(name = "ASSIST_DEPT_NAME", length = 70)
  var assistDeptName: java.lang.String = _
  @Column(name = "ASSIST_DATE", length = 8)
  var assistDate: java.lang.String = _
  @Column(name = "ASSIST_CONTACTS", length = 30)
  var assistContacts: java.lang.String = _
  @Column(name = "ASSIST_NUMBER", length = 40)
  var assistNumber: java.lang.String = _
  @Column(name = "ASSIST_APPROVAL", length = 30)
  var assistApproval: java.lang.String = _
  @Column(name = "ASSIST_SIGN", length = 1)
  var assistSign: java.lang.String = _
  @Column(name = "GATHERDEPARTNAME", length = 100)
  var gatherdepartname: java.lang.String = _
  @Column(name = "GATHERUSERNAME", length = 100)
  var gatherusername: java.lang.String = _
  @Column(name = "CASE_NUMBER", length = 23)
  var caseNumber: java.lang.String = _
  @Column(name = "DETAIN_FLAG", length = 1)
  var detainFlag: java.lang.String = _
  @Column(name = "REUSE_STATUS", length = 1)
  var reuseStatus: java.lang.String = _
  @Column(name = "OLD_PERSONID", length = 23)
  var oldPersonid: java.lang.String = _
  @Column(name = "PSIS_NO", length = 32)
  var jingZongPersonId: java.lang.String = _
  @Column(name = "XZ_PERSON_ID", length = 32)
  var casePersonid: java.lang.String = _
  @Column(name = "GATHERUSERSHENFENID", length = 18)
  var chopPersonIdCard: java.lang.String = _
  @Column(name = "GATHERUSERPHONE", length = 18)
  var chopPersonTel: java.lang.String = _
  @Column(name = "PERSON_TYPE", length = 20)
  var captureInfoReasonCode: java.lang.String = _
  @Column(name = "INCOMPLETEFINGERCODE", length = 39)
  var inCompleteFingerCode: java.lang.String = _

  def this(personid: java.lang.String) {
    this()
    this.personid = personid
  }

  def this(personid: java.lang.String, idcardno: java.lang.String, name: java.lang.String, spellname: java.lang.String, usedname: java.lang.String, usedspell: java.lang.String, aliasname: java.lang.String, aliasspell: java.lang.String, sexCode: java.lang.String, nativeplaceCode: java.lang.String, nationCode: java.lang.String, ifmarryCode: java.lang.String, toneCode: java.lang.String, tone: java.lang.String, birthdayst: java.util.Date, birthdayed: java.util.Date, birthCode: java.lang.String, birthStreet: java.lang.String, birthdetail: java.lang.String, door: java.lang.String, doorStreet: java.lang.String, doordetail: java.lang.String, address: java.lang.String, addressStreet: java.lang.String, addressdetail: java.lang.String, cultureCode: java.lang.String, sourceincomeCode: java.lang.String, faithCode: java.lang.String, haveemployment: java.lang.String, jobCode: java.lang.String, headship: java.lang.String, employunit: java.lang.String, employaddress: java.lang.String, otherspecialty: java.lang.String, specialidentityCode: java.lang.String, politicsCode: java.lang.String, istransientpop: java.lang.String, istempregist: java.lang.String, havepermit: java.lang.String, haveresidence: java.lang.String, isservice: java.lang.String, specialgroupCode: java.lang.String, haveseparation: java.lang.String, ismigrantworker: java.lang.String, nameofschool: java.lang.String, istraining: java.lang.String, havecertificate: java.lang.String, staturest: java.lang.Short, avoirdupois: java.math.BigDecimal, footsize: java.math.BigDecimal, shoelength: java.math.BigDecimal, bodilyformCode: java.lang.String, faceformCode: java.lang.String, iseyeglass: java.lang.String, shoesize: java.lang.Short, bloodtypeCode: java.lang.String, gatherOrgCode: java.lang.String, ipaddress: java.lang.String, gathererId: java.lang.String, gatherDate: java.util.Date, gatherTypeId: java.lang.String, status: java.lang.String, isfingerrepeat: java.lang.String, fingerrepeatno: java.lang.String, taskSource: java.lang.String, receiveTime: java.util.Date, isreturn: java.lang.String, returnTime: java.util.Date, annex: java.lang.String, inputpsn: java.lang.String, inputtime: java.util.Date, modifiedpsn: java.lang.String, modifiedtime: java.util.Date, deletag: java.lang.String, schedule: java.lang.String, approval: java.lang.String, dnaCode: java.lang.String, gatherCategory: java.lang.String, personCategory: java.lang.String, auditor: java.lang.String, auditedtime: java.util.Date, isregather: java.lang.String, gatherFingerMode: java.lang.String, caseName: java.lang.String, caseClasses: java.lang.String, reason: java.lang.String, gatherFingerNum: java.math.BigDecimal, fingerRemark: java.lang.String, deprtmac: java.lang.String, gatherdepartcode: java.lang.String, gatheruserid: java.lang.String, gatherFingerTime: java.util.Date, isSendTl: java.math.BigDecimal, caseBriefContents: java.lang.String, pushStatus: java.lang.String, pushDate: java.util.Date, remark: java.lang.String, dataSources: java.lang.String, fingershowStatus: java.lang.Short, cityCode: java.lang.String, delayDeadline: java.util.Date, fptGatherDepartCode: java.lang.String, fptGatherDepartName: java.lang.String, sid: java.lang.Long, blowCode: java.lang.String, blowStreet: java.lang.String, blowDetail: java.lang.String, blowLongitude: java.lang.String, blowLatitude: java.lang.String, blowEastwest: java.lang.Character, blowNorthsouth: java.lang.Character, seq: java.math.BigDecimal, cardid: java.lang.String, recordmark: java.lang.Character, recordsituation: java.lang.String, validDate: java.util.Date, arriveLocalDate: java.util.Date, leaveLocalDate: java.util.Date, dbSource: java.lang.String, dbSourceDis: java.lang.String, jobDes: java.lang.String, isXjssmz: java.lang.Character, passportNum: java.lang.String, countryCode: java.lang.String, foreignName: java.lang.String, passportValidDate: java.util.Date, visaPlace: java.lang.String, passportType: java.lang.String, visaDate: java.util.Date, assistLevel: java.lang.String, assistBonus: java.lang.String, assistPurpose: java.lang.String, assistRefPerson: java.lang.String, assistRefCase: java.lang.String, assistValidDate: java.lang.String, assistExplain: java.lang.String, assistDeptCode: java.lang.String, assistDeptName: java.lang.String, assistDate: java.lang.String, assistContacts: java.lang.String, assistNumber: java.lang.String, assistApproval: java.lang.String, assistSign: java.lang.String, gatherdepartname: java.lang.String, gatherusername: java.lang.String, caseNumber: java.lang.String, detainFlag: java.lang.String, reuseStatus: java.lang.String, oldPersonid: java.lang.String, jingZongPersonId: java.lang.String, casePersonid: java.lang.String, chopPersonIdCard: java.lang.String, chopPersonTel: java.lang.String, captureInfoReasonCode: java.lang.String, inCompleteFingerCode: java.lang.String) {
    this()
    this.personid = personid
    this.idcardno = idcardno
    this.name = name
    this.spellname = spellname
    this.usedname = usedname
    this.usedspell = usedspell
    this.aliasname = aliasname
    this.aliasspell = aliasspell
    this.sexCode = sexCode
    this.nativeplaceCode = nativeplaceCode
    this.nationCode = nationCode
    this.ifmarryCode = ifmarryCode
    this.toneCode = toneCode
    this.tone = tone
    this.birthdayst = birthdayst
    this.birthdayed = birthdayed
    this.birthCode = birthCode
    this.birthStreet = birthStreet
    this.birthdetail = birthdetail
    this.door = door
    this.doorStreet = doorStreet
    this.doordetail = doordetail
    this.address = address
    this.addressStreet = addressStreet
    this.addressdetail = addressdetail
    this.cultureCode = cultureCode
    this.sourceincomeCode = sourceincomeCode
    this.faithCode = faithCode
    this.haveemployment = haveemployment
    this.jobCode = jobCode
    this.headship = headship
    this.employunit = employunit
    this.employaddress = employaddress
    this.otherspecialty = otherspecialty
    this.specialidentityCode = specialidentityCode
    this.politicsCode = politicsCode
    this.istransientpop = istransientpop
    this.istempregist = istempregist
    this.havepermit = havepermit
    this.haveresidence = haveresidence
    this.isservice = isservice
    this.specialgroupCode = specialgroupCode
    this.haveseparation = haveseparation
    this.ismigrantworker = ismigrantworker
    this.nameofschool = nameofschool
    this.istraining = istraining
    this.havecertificate = havecertificate
    this.staturest = staturest
    this.avoirdupois = avoirdupois
    this.footsize = footsize
    this.shoelength = shoelength
    this.bodilyformCode = bodilyformCode
    this.faceformCode = faceformCode
    this.iseyeglass = iseyeglass
    this.shoesize = shoesize
    this.bloodtypeCode = bloodtypeCode
    this.gatherOrgCode = gatherOrgCode
    this.ipaddress = ipaddress
    this.gathererId = gathererId
    this.gatherDate = gatherDate
    this.gatherTypeId = gatherTypeId
    this.status = status
    this.isfingerrepeat = isfingerrepeat
    this.fingerrepeatno = fingerrepeatno
    this.taskSource = taskSource
    this.receiveTime = receiveTime
    this.isreturn = isreturn
    this.returnTime = returnTime
    this.annex = annex
    this.inputpsn = inputpsn
    this.inputtime = inputtime
    this.modifiedpsn = modifiedpsn
    this.modifiedtime = modifiedtime
    this.deletag = deletag
    this.schedule = schedule
    this.approval = approval
    this.dnaCode = dnaCode
    this.gatherCategory = gatherCategory
    this.personCategory = personCategory
    this.auditor = auditor
    this.auditedtime = auditedtime
    this.isregather = isregather
    this.gatherFingerMode = gatherFingerMode
    this.caseName = caseName
    this.caseClasses = caseClasses
    this.reason = reason
    this.gatherFingerNum = gatherFingerNum
    this.fingerRemark = fingerRemark
    this.deprtmac = deprtmac
    this.gatherdepartcode = gatherdepartcode
    this.gatheruserid = gatheruserid
    this.gatherFingerTime = gatherFingerTime
    this.isSendTl = isSendTl
    this.caseBriefContents = caseBriefContents
    this.pushStatus = pushStatus
    this.pushDate = pushDate
    this.remark = remark
    this.dataSources = dataSources
    this.fingershowStatus = fingershowStatus
    this.cityCode = cityCode
    this.delayDeadline = delayDeadline
    this.fptGatherDepartCode = fptGatherDepartCode
    this.fptGatherDepartName = fptGatherDepartName
    this.sid = sid
    this.blowCode = blowCode
    this.blowStreet = blowStreet
    this.blowDetail = blowDetail
    this.blowLongitude = blowLongitude
    this.blowLatitude = blowLatitude
    this.blowEastwest = blowEastwest
    this.blowNorthsouth = blowNorthsouth
    this.seq = seq
    this.cardid = cardid
    this.recordmark = recordmark
    this.recordsituation = recordsituation
    this.validDate = validDate
    this.arriveLocalDate = arriveLocalDate
    this.leaveLocalDate = leaveLocalDate
    this.dbSource = dbSource
    this.dbSourceDis = dbSourceDis
    this.jobDes = jobDes
    this.isXjssmz = isXjssmz
    this.passportNum = passportNum
    this.countryCode = countryCode
    this.foreignName = foreignName
    this.passportValidDate = passportValidDate
    this.visaPlace = visaPlace
    this.passportType = passportType
    this.visaDate = visaDate
    this.assistLevel = assistLevel
    this.assistBonus = assistBonus
    this.assistPurpose = assistPurpose
    this.assistRefPerson = assistRefPerson
    this.assistRefCase = assistRefCase
    this.assistValidDate = assistValidDate
    this.assistExplain = assistExplain
    this.assistDeptCode = assistDeptCode
    this.assistDeptName = assistDeptName
    this.assistDate = assistDate
    this.assistContacts = assistContacts
    this.assistNumber = assistNumber
    this.assistApproval = assistApproval
    this.assistSign = assistSign
    this.gatherdepartname = gatherdepartname
    this.gatherusername = gatherusername
    this.caseNumber = caseNumber
    this.detainFlag = detainFlag
    this.reuseStatus = reuseStatus
    this.oldPersonid = oldPersonid
    this.jingZongPersonId = jingZongPersonId
    this.casePersonid = casePersonid
    this.chopPersonIdCard = chopPersonIdCard
    this.chopPersonTel = chopPersonTel
    this.captureInfoReasonCode = captureInfoReasonCode
    this.inCompleteFingerCode = inCompleteFingerCode
  }

}