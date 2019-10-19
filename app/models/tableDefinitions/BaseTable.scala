package models.tableDefinitions

import models.BaseEntity
import org.checkerframework.checker.units.qual.{A, K}
import slick.jdbc.MySQLProfile.api._



abstract class BaseTable  [E <: BaseEntity[_]  ](tag: Tag, tableName: String) extends Table[E](tag, tableName)  {

   // todo  add created at , updated at

}

trait  TableId [P] {
  def id: Rep[P]
}

