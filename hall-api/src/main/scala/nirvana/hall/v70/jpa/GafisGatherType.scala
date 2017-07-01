package nirvana.hall.v70.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table, Temporal, TemporalType}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * GafisGatherType generated by hall orm 
 */
object GafisGatherType extends ActiveRecordInstance[GafisGatherType]

@Entity
@Table(name = "GAFIS_GATHER_TYPE"
)
class GafisGatherType extends ActiveRecord {


  @Id
  @Column(name = "PK_ID", unique = true, nullable = false, length = 32)
  var pkId: java.lang.String = _
  @Column(name = "TYPE_NAME", nullable = false, length = 60)
  var typeName: java.lang.String = _
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
  @Column(name = "MENU_ID", length = 10)
  var menuId: java.lang.String = _
  @Column(name = "PERSON_CATEGORY", length = 3)
  var personCategory: java.lang.String = _
  @Column(name = "GATHER_CATEGORY", length = 1)
  var gatherCategory: java.lang.String = _
  @Column(name = "PARENT_ID", length = 32)
  var parentId: java.lang.String = _
  @Column(name = "ISCHILDREN", length = 1)
  var ischildren: java.lang.String = _
  @Column(name = "RULE_ID", length = 32)
  var ruleId: java.lang.String = _
  @Column(name = "CARD_RULE_ID", length = 32)
  var cardRuleId: java.lang.String = _
  @Column(name = "CARD_RULE_TYPE", length = 1)
  var cardRuleType: java.lang.String = _


  def this(pkId: java.lang.String, typeName: java.lang.String, createUserId: java.lang.String, createDatetime: java.util.Date) {
    this()
    this.pkId = pkId
    this.typeName = typeName
    this.createUserId = createUserId
    this.createDatetime = createDatetime
  }

  def this(pkId: java.lang.String, typeName: java.lang.String, deleteFlag: java.lang.Long, createUserId: java.lang.String, createDatetime: java.util.Date, updateUserId: java.lang.String, updateDatetime: java.util.Date, menuId: java.lang.String, personCategory: java.lang.String, gatherCategory: java.lang.String, parentId: java.lang.String, ischildren: java.lang.String, ruleId: java.lang.String, cardRuleId: java.lang.String, cardRuleType: java.lang.String) {
    this()
    this.pkId = pkId
    this.typeName = typeName
    this.deleteFlag = deleteFlag
    this.createUserId = createUserId
    this.createDatetime = createDatetime
    this.updateUserId = updateUserId
    this.updateDatetime = updateDatetime
    this.menuId = menuId
    this.personCategory = personCategory
    this.gatherCategory = gatherCategory
    this.parentId = parentId
    this.ischildren = ischildren
    this.ruleId = ruleId
    this.cardRuleId = cardRuleId
    this.cardRuleType = cardRuleType
  }


}

