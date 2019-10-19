package repositories

   import models.BaseEntity
   import models.tableDefinitions.{BaseTable, TableId}
   import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
   import slick.driver.JdbcProfile
   import slick.jdbc.MySQLProfile.api._
   import slick.lifted.TableQuery
   import scala.concurrent.ExecutionContext


abstract class  BaseRepository[ E <: BaseEntity[C], T <: BaseTable[E] with TableId[C] , C: BaseColumnType ]
(val dbConfigProvider: DatabaseConfigProvider, executionContext: ExecutionContext, query: TableQuery[T]) extends HasDatabaseConfigProvider[JdbcProfile] {

 // def filter[F <: Rep[_]](expr: T => F)(implicit wt: CanBeQueryCondition[F]): DBIO[Seq[E]] = query.filter(expr).result    //custom filter method ? todo

  def all(): DBIO[Seq[E]] = query.result //fetch all entities from database

  def find(id: C): DBIO[Option[E]] = query.filter(_.id === id).result.headOption //get one entity by its id

  def insert(entity: E): DBIO[C] = query.returning(query.map(_.id)) += entity // insert entity in database and return its id

  def deleteById(id: C) = query.filter(_.id === id).delete // delete entity by id

  def count: DBIO[Int] = query.size.result // returns number of entities

  def save(model: E)(implicit ec: ExecutionContext): DBIO[Option[E]] = {
    insert(model).flatMap{ id =>  find(id) }.transactionally
  }     // insert model and then return the saved model

  def create(model: E)(implicit  ec : ExecutionContext ) : DBIO[E] = {
  query.insertOrUpdate(model).map(_ => model)
  } // for saving entities with non auto increment primary keys (like session )


  //update model and return the updated model

   def update(id : C , model: E)(implicit ec: ExecutionContext): DBIO[Option[E]] = {
     query.filter(_.id === model.id).update(model).map{
       case 0 => None
       case _ => Some(model)
     }
   }

  def saveAll(models: E*)  = query ++= models //todo ?


}




