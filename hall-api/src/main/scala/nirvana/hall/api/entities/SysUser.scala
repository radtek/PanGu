package nirvana.hall.api.entities

import scalikejdbc._
import org.joda.time.{DateTime}

case class SysUser(
  pkId: String,
  loginName: String,
  password: Option[String] = None,
  email: Option[String] = None,
  trueName: Option[String] = None,
  deleteFlag: Option[String] = None,
  remark: Option[String] = None,
  createDatetime: Option[DateTime] = None,
  updateDatetime: Option[DateTime] = None,
  createUserId: Option[String] = None,
  updateUserId: Option[String] = None,
  departCode: Option[String] = None,
  idcard: Option[String] = None,
  policeNumber: Option[String] = None,
  genderCode: Option[String] = None,
  userType: Option[String] = None,
  phone: Option[String] = None,
  departStartDate: Option[DateTime] = None,
  userStatus: Option[String] = None,
  lastLoginDate: Option[DateTime] = None,
  loginTimeControl: Option[String] = None) {

  def save()(implicit session: DBSession = SysUser.autoSession): SysUser = SysUser.save(this)(session)

  def destroy()(implicit session: DBSession = SysUser.autoSession): Unit = SysUser.destroy(this)(session)

}


object SysUser extends SQLSyntaxSupport[SysUser] {

  override val tableName = "SYS_USER"

  override val columns = Seq("PK_ID", "LOGIN_NAME", "PASSWORD", "EMAIL", "TRUE_NAME", "DELETE_FLAG", "REMARK", "CREATE_DATETIME", "UPDATE_DATETIME", "CREATE_USER_ID", "UPDATE_USER_ID", "DEPART_CODE", "IDCARD", "POLICE_NUMBER", "GENDER_CODE", "USER_TYPE", "PHONE", "DEPART_START_DATE", "USER_STATUS", "LAST_LOGIN_DATE", "LOGIN_TIME_CONTROL")

  def apply(su: SyntaxProvider[SysUser])(rs: WrappedResultSet): SysUser = apply(su.resultName)(rs)
  def apply(su: ResultName[SysUser])(rs: WrappedResultSet): SysUser = new SysUser(
    pkId = rs.get(su.pkId),
    loginName = rs.get(su.loginName),
    password = rs.get(su.password),
    email = rs.get(su.email),
    trueName = rs.get(su.trueName),
    deleteFlag = rs.get(su.deleteFlag),
    remark = rs.get(su.remark),
    createDatetime = rs.get(su.createDatetime),
    updateDatetime = rs.get(su.updateDatetime),
    createUserId = rs.get(su.createUserId),
    updateUserId = rs.get(su.updateUserId),
    departCode = rs.get(su.departCode),
    idcard = rs.get(su.idcard),
    policeNumber = rs.get(su.policeNumber),
    genderCode = rs.get(su.genderCode),
    userType = rs.get(su.userType),
    phone = rs.get(su.phone),
    departStartDate = rs.get(su.departStartDate),
    userStatus = rs.get(su.userStatus),
    lastLoginDate = rs.get(su.lastLoginDate),
    loginTimeControl = rs.get(su.loginTimeControl)
  )

  val su = SysUser.syntax("su")

 override def autoSession = nirvana.hall.api.services.AutoSpringDataSourceSession()

  def find(pkId: String)(implicit session: DBSession = autoSession): Option[SysUser] = {
    withSQL {
      select.from(SysUser as su).where.eq(su.pkId, pkId)
    }.map(SysUser(su.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[SysUser] = {
    withSQL(select.from(SysUser as su)).map(SysUser(su.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    withSQL(select(sqls.count).from(SysUser as su)).map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[SysUser] = {
    withSQL {
      select.from(SysUser as su).where.append(where)
    }.map(SysUser(su.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[SysUser] = {
    withSQL {
      select.from(SysUser as su).where.append(where)
    }.map(SysUser(su.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(SysUser as su).where.append(where)
    }.map(_.long(1)).single.apply().get
  }

  def create(
    pkId: String,
    loginName: String,
    password: Option[String] = None,
    email: Option[String] = None,
    trueName: Option[String] = None,
    deleteFlag: Option[String] = None,
    remark: Option[String] = None,
    createDatetime: Option[DateTime] = None,
    updateDatetime: Option[DateTime] = None,
    createUserId: Option[String] = None,
    updateUserId: Option[String] = None,
    departCode: Option[String] = None,
    idcard: Option[String] = None,
    policeNumber: Option[String] = None,
    genderCode: Option[String] = None,
    userType: Option[String] = None,
    phone: Option[String] = None,
    departStartDate: Option[DateTime] = None,
    userStatus: Option[String] = None,
    lastLoginDate: Option[DateTime] = None,
    loginTimeControl: Option[String] = None)(implicit session: DBSession = autoSession): SysUser = {
    withSQL {
      insert.into(SysUser).columns(
        column.pkId,
        column.loginName,
        column.password,
        column.email,
        column.trueName,
        column.deleteFlag,
        column.remark,
        column.createDatetime,
        column.updateDatetime,
        column.createUserId,
        column.updateUserId,
        column.departCode,
        column.idcard,
        column.policeNumber,
        column.genderCode,
        column.userType,
        column.phone,
        column.departStartDate,
        column.userStatus,
        column.lastLoginDate,
        column.loginTimeControl
      ).values(
        pkId,
        loginName,
        password,
        email,
        trueName,
        deleteFlag,
        remark,
        createDatetime,
        updateDatetime,
        createUserId,
        updateUserId,
        departCode,
        idcard,
        policeNumber,
        genderCode,
        userType,
        phone,
        departStartDate,
        userStatus,
        lastLoginDate,
        loginTimeControl
      )
    }.update.apply()

    SysUser(
      pkId = pkId,
      loginName = loginName,
      password = password,
      email = email,
      trueName = trueName,
      deleteFlag = deleteFlag,
      remark = remark,
      createDatetime = createDatetime,
      updateDatetime = updateDatetime,
      createUserId = createUserId,
      updateUserId = updateUserId,
      departCode = departCode,
      idcard = idcard,
      policeNumber = policeNumber,
      genderCode = genderCode,
      userType = userType,
      phone = phone,
      departStartDate = departStartDate,
      userStatus = userStatus,
      lastLoginDate = lastLoginDate,
      loginTimeControl = loginTimeControl)
  }

  def save(entity: SysUser)(implicit session: DBSession = autoSession): SysUser = {
    withSQL {
      update(SysUser).set(
        column.pkId -> entity.pkId,
        column.loginName -> entity.loginName,
        column.password -> entity.password,
        column.email -> entity.email,
        column.trueName -> entity.trueName,
        column.deleteFlag -> entity.deleteFlag,
        column.remark -> entity.remark,
        column.createDatetime -> entity.createDatetime,
        column.updateDatetime -> entity.updateDatetime,
        column.createUserId -> entity.createUserId,
        column.updateUserId -> entity.updateUserId,
        column.departCode -> entity.departCode,
        column.idcard -> entity.idcard,
        column.policeNumber -> entity.policeNumber,
        column.genderCode -> entity.genderCode,
        column.userType -> entity.userType,
        column.phone -> entity.phone,
        column.departStartDate -> entity.departStartDate,
        column.userStatus -> entity.userStatus,
        column.lastLoginDate -> entity.lastLoginDate,
        column.loginTimeControl -> entity.loginTimeControl
      ).where.eq(column.pkId, entity.pkId)
    }.update.apply()
    entity
  }

  def destroy(entity: SysUser)(implicit session: DBSession = autoSession): Unit = {
    withSQL { delete.from(SysUser).where.eq(column.pkId, entity.pkId) }.update.apply()
  }

}
