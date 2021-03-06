package nirvana.hall.webservice.jpa.survey

// Generated 2018-7-24 14:53:35 by Stark Activerecord generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance};

/**
 * LogGetReceptionno generated by stark activerecord generator
 */
object LogGetReceptionno extends ActiveRecordInstance[LogGetReceptionno]

@Entity
@Table(name="LOG_GET_RECEPTIONNO"
)
class LogGetReceptionno extends ActiveRecord {


    @Id
    @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
    var pkId: java.lang.String = _
    @Column(name = "USERNAME", length = 20)
    var username: java.lang.String = _
    @Column(name = "XCKYBH", length = 23)
    var xckybh: java.lang.String = _
    @Column(name = "CALLTIME")
    var calltime: java.util.Date = _
    @Column(name = "RETURN_JJBH", length = 30)
    var returnJjbh: java.lang.String = _
    @Column(name = "RETURNTIME")
    var returntime: java.util.Date = _
    @Column(name = "ERRORMSG")
    var errormsg: java.lang.String = _


	
    def this(pkId:java.lang.String) {
        this()
        this.pkId = pkId
    }
    def this(pkId:java.lang.String, username:java.lang.String, xckybh:java.lang.String, calltime:java.util.Date, returnJjbh:java.lang.String, returntime:java.util.Date, errormsg:java.lang.String) {
       this()
       this.pkId = pkId
       this.username = username
       this.xckybh = xckybh
       this.calltime = calltime
       this.returnJjbh = returnJjbh
       this.returntime = returntime
       this.errormsg = errormsg
    }
   




}


