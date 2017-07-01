package nirvana.hall.v70.jpa

;
// Generated Jan 6, 2016 6:08:10 PM by hall orm generator 4.3.1.Final


import javax.persistence.{Column, Entity, Id, Table}

import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
 * SysMenu generated by hall orm 
 */
object SysMenu extends ActiveRecordInstance[SysMenu]

@Entity
@Table(name = "SYS_MENU"
)
class SysMenu extends ActiveRecord {


  @Id
  @Column(name = "MENU_CODE", unique = true, nullable = false, length = 10)
  var menuCode: java.lang.String = _
  @Column(name = "SYS_CODE", length = 2)
  var sysCode: java.lang.String = _
  @Column(name = "MENU_NAME", length = 60)
  var menuName: java.lang.String = _
  @Column(name = "MENU_ACTION", length = 100)
  var menuAction: java.lang.String = _
  @Column(name = "DELETE_FLAG")
  var deleteFlag: java.lang.Long = _
  @Column(name = "ICON", length = 100)
  var icon: java.lang.String = _
  @Column(name = "PARENT_ID", length = 10)
  var parentId: java.lang.String = _
  @Column(name = "LEVEL_NO")
  var levelNo: java.lang.Long = _
  @Column(name = "ORD")
  var ord: java.lang.Long = _
  @Column(name = "IS_LEAF")
  var isLeaf: java.lang.Long = _
  @Column(name = "SOFA", length = 60)
  var sofa: java.lang.String = _
  @Column(name = "BENCH", length = 60)
  var bench: java.lang.String = _
  @Column(name = "WIN_PANEL", length = 60)
  var winPanel: java.lang.String = _


  def this(menuCode: java.lang.String) {
    this()
    this.menuCode = menuCode
  }

  def this(menuCode: java.lang.String, sysCode: java.lang.String, menuName: java.lang.String, menuAction: java.lang.String, deleteFlag: java.lang.Long, icon: java.lang.String, parentId: java.lang.String, levelNo: java.lang.Long, ord: java.lang.Long, isLeaf: java.lang.Long, sofa: java.lang.String, bench: java.lang.String, winPanel: java.lang.String) {
    this()
    this.menuCode = menuCode
    this.sysCode = sysCode
    this.menuName = menuName
    this.menuAction = menuAction
    this.deleteFlag = deleteFlag
    this.icon = icon
    this.parentId = parentId
    this.levelNo = levelNo
    this.ord = ord
    this.isLeaf = isLeaf
    this.sofa = sofa
    this.bench = bench
    this.winPanel = winPanel
  }


}

