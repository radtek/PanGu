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
 * LogGetCaseno generated by stark activerecord generator
 */
object LogGetCaseno extends ActiveRecordInstance[LogGetCaseno]

@Entity
@Table(name="LOG_GET_CASENO"
)
class LogGetCaseno extends ActiveRecord {


    @Id
    @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
    var pkId: java.lang.String = _
    @Column(name = "USERNAME", length = 20)
    var username: java.lang.String = _
    @Column(name = "XCKYBH", length = 23)
    var xckybh: java.lang.String = _
    @Column(name = "CALLTIME")
    var calltime: java.util.Date = _
    @Column(name = "RETURN_ASJBH", length = 23)
    var returnAsjbh: java.lang.String = _
    @Column(name = "RETURNTIME")
    var returntime: java.util.Date = _
    @Column(name = "ERRORMSG")
    var errormsg: java.lang.String = _


	
    def this(pkId:java.lang.String) {
        this()
        this.pkId = pkId
    }
    def this(pkId:java.lang.String, username:java.lang.String, xckybh:java.lang.String, calltime:java.util.Date, returnAsjbh:java.lang.String, returntime:java.util.Date, errormsg:java.lang.String) {
       this()
       this.pkId = pkId
       this.username = username
       this.xckybh = xckybh
       this.calltime = calltime
       this.returnAsjbh = returnAsjbh
       this.returntime = returntime
       this.errormsg = errormsg
    }
   




}


