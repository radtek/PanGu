package nirvana.hall.webservice.jpa.survey

// Generated 2018-7-24 14:53:35 by Stark Activerecord generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance};

/**
 * LogPutfingerstatus generated by stark activerecord generator
 */
object LogHaiXinservicestatus extends ActiveRecordInstance[LogHaiXinservicestatus]

@Entity
@Table(name="LOG_HAIXINSERVICESTATUS"
)
class LogHaiXinservicestatus extends ActiveRecord {

    @Id
    @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
    var pkId: java.lang.String = _
    @Column(name = "INTERFACEADDR", length = 200)
    var interfaceaddr: java.lang.String = _
    @Column(name = "STATUS", length = 1)
    var status: java.lang.Integer = _
    @Column(name = "ASJFSDD_XZQHDM", length = 12)
    var asjfsddXzqhdm: java.lang.Integer = _
    @Column(name = "CALLTIME")
    var calltime: java.util.Date = _
	
    def this(pkId:java.lang.String) {
        this()
        this.pkId = pkId
    }
    def this(pkId:java.lang.String, interfaceaddr:java.lang.String, status:java.lang.Integer, asjfsddXzqhdm:java.lang.Integer, calltime:java.util.Date) {
       this()
       this.pkId = pkId
       this.interfaceaddr = interfaceaddr
       this.status = status
       this.asjfsddXzqhdm = asjfsddXzqhdm
       this.calltime = calltime
    }

}


