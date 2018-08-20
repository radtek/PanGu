package nirvana.hall.webservice.jpa

// Generated 2018-7-24 14:53:35 by Stark Activerecord generator 4.3.1.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import stark.activerecord.services.ActiveRecord;
import stark.activerecord.services.ActiveRecordInstance;

/**
 * LogPuthitresult generated by stark activerecord generator
 */
object LogPuthitresultDetail extends ActiveRecordInstance[LogPuthitresultDetail]

@Entity
@Table(name="LOG_PUTHITRESULT"
)
class LogPuthitresultDetail extends ActiveRecord {


    @Id
    @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
    var pkId: java.lang.String = _
    @Column(name = "FID", length = 32)
    var fId: java.lang.String = _
    @Column(name = "USERNAME", length = 20)
    var username: java.lang.String = _
    @Column(name = "HITRESULT_TYPE", length = 2)
    var hitresultType: java.lang.String = _
    @Column(name = "XCZW_ASJBH", length = 30)
    var xczwAsjbh: java.lang.String = _
    @Column(name = "XCZW_YSXT_ASJBH", length = 23)
    var xczwYsxtAsjbh: java.lang.String = _
    @Column(name = "XCZW_XCKYBH", length = 23)
    var xczwXckybh: java.lang.String = _
    @Column(name = "XCZW_YSXT_XCZZHWBH", length = 30)
    var xczwYsxtXczzhwbh: java.lang.String = _
    @Column(name = "XCZW_XCWZBH", length = 30)
    var xczwXcwzbh: java.lang.String = _
    @Column(name = "XCZW_XCZZHWKBH", length = 23)
    var xczwXczzhwkbh: java.lang.String = _
    @Column(name = "NYZW_YSXT_ASJXGRYBH", length = 23)
    var nyzwYsxtAsjxgrybh: java.lang.String = _
    @Column(name = "NYZW_JZRYBH", length = 23)
    var nyzwJzrybh: java.lang.String = _
    @Column(name = "NYZW_ASJXGRYBH", length = 23)
    var nyzwAsjxgrybh: java.lang.String = _
    @Column(name = "NYZW_ZZHWKBH", length = 23)
    var nyzwZzhwkbh: java.lang.String = _
    @Column(name = "NYZW_ZZHWDM", length = 2)
    var nyzwZzhwdm: java.lang.String = _
    @Column(name = "ASJFSDD_XZQHDM", precision = 12, scale = 0)
    var asjfsddXzqhdm: java.lang.Long = _
    @Temporal(TemporalType.DATE)
    @Column(name = "CALLTIME", length = 10)
    var calltime: java.util.Date = _
    @Temporal(TemporalType.DATE)
    @Column(name = "RETURNTIME", length = 10)
    var returntime: java.util.Date = _
    @Column(name = "FPTPATH", length = 500)
    var fptpath: java.lang.String = _
    @Column(name = "RETURNSTATUS", length = 3)
    var returnstatus: java.lang.String = _
    @Column(name = "ERRORMSG")
    var errormsg: java.lang.String = _


	
    def this(pkId:java.lang.String) {
        this()
        this.pkId = pkId
    }
    def this(pkId:java.lang.String, fId:java.lang.String, username:java.lang.String, hitresultType:java.lang.String, xczwAsjbh:java.lang.String, xczwYsxtAsjbh:java.lang.String, xczwXckybh:java.lang.String, xczwYsxtXczzhwbh:java.lang.String, xczwXcwzbh:java.lang.String, xczwXczzhwkbh:java.lang.String, nyzwYsxtAsjxgrybh:java.lang.String, nyzwJzrybh:java.lang.String, nyzwAsjxgrybh:java.lang.String, nyzwZzhwkbh:java.lang.String, nyzwZzhwdm:java.lang.String, asjfsddXzqhdm:java.lang.Long, calltime:java.util.Date, returntime:java.util.Date, fptpath:java.lang.String, returnstatus:java.lang.String, errormsg:String) {
       this()
       this.pkId = pkId
       this.fId = fId
       this.username = username
       this.hitresultType = hitresultType
       this.xczwAsjbh = xczwAsjbh
       this.xczwYsxtAsjbh = xczwYsxtAsjbh
       this.xczwXckybh = xczwXckybh
       this.xczwYsxtXczzhwbh = xczwYsxtXczzhwbh
       this.xczwXcwzbh = xczwXcwzbh
       this.xczwXczzhwkbh = xczwXczzhwkbh
       this.nyzwYsxtAsjxgrybh = nyzwYsxtAsjxgrybh
       this.nyzwJzrybh = nyzwJzrybh
       this.nyzwAsjxgrybh = nyzwAsjxgrybh
       this.nyzwZzhwkbh = nyzwZzhwkbh
       this.nyzwZzhwdm = nyzwZzhwdm
       this.asjfsddXzqhdm = asjfsddXzqhdm
       this.calltime = calltime
       this.returntime = returntime
       this.fptpath = fptpath
       this.returnstatus = returnstatus
       this.errormsg = errormsg
    }
   




}


