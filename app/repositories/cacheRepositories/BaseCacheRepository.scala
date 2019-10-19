package repositories.cacheRepositories

import javax.inject.Inject
import play.api.cache.AsyncCacheApi
import repositories.SessionRepository

import scala.concurrent.ExecutionContext

class BaseCacheRepository @Inject()(cache:AsyncCacheApi, sessionRepository: SessionRepository)(implicit executionContext: ExecutionContext)   {

// val  delegate : BaseRepository[models.Session, SessionTableDef,String]




  /**
   *
   * try to get data from cache , if not found delegate to databaseRepo , get data from database and save in cache
   *
   *
   *
   */













}
