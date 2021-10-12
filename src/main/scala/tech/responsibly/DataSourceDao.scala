package tech.responsibly

import DataSourceDao._
import anorm.{ RowParser, SqlParser, SqlStringInterpolation }

import java.sql.Connection
import scala.concurrent.{ ExecutionContext, Future }

class DataSourceDao(jdbcDataSource: javax.sql.DataSource)(implicit ec: ExecutionContext) {

  private val dataSourceRowParser: RowParser[(String,String,String,String,String)] = {
    for {
      data_source_id   <- SqlParser.get[String]("data_source_id")
      data_source_name <- SqlParser.get[String]("data_source_name")
      parameter_id <- SqlParser.get[String]("parameter_id")
      parameter_name <- SqlParser.get[String]("parameter_name")
      parameter_category_id <- SqlParser.get[String]("parameter_category_id")
    } yield (data_source_id, data_source_name, parameter_id, parameter_name, parameter_category_id)
  }

  private val categoryParser: RowParser[(String, String, String)] = {
    for {
      id <- SqlParser.get[String]("id")
      name <- SqlParser.get[String]("name")
      parent_id <- SqlParser.get[String]("parent_id")
    } yield (id, name, parent_id)
  }

  def getById(id: String): Future[Option[DataSource]] = {
    withConnection { implicit connection =>
      SQL"""
      SELECT
        ds.id as data_source_id,
        ds.name as data_source_name,
        p.id as parameter_id,
        p.name AS parameter_name,
        p.category_id AS parameter_category_id
      FROM data_sources ds
      LEFT JOIN data_source_parameters dsp ON (ds.id = dsp.data_source_id)
      LEFT JOIN parameters p ON (dsp.parameter_id = p.id)
      LEFT JOIN
      WHERE ds.id = $id
      """.as(dataSourceRowParser.*)
    }.map(rows =>
      rows.groupBy{
        case (dsId, dsName, _, _, _) => (dsId, dsName)
      }.view.mapValues(l => l.map{case (_,_,pId, pName, pCategoryId) => (pId, pName, pCategoryId)}.toSet)
        .toList
        .map { case ((dsId, dsName), parameters) =>
          Future.sequence(parameters.map{case (pId, pName, pCategoryId) =>
            getCategoryById(pCategoryId).map(category => Parameter(pId, pName, category))})
            .map(parameters => DataSource(dsId, dsName, parameters))
        })
      .map(Future.sequence(_))
      .flatten
      .map(_.headOption)
  }


  def getCategoryById(id: String): Future[Category] = {
    withConnection { implicit connection =>
      SQL"""
      with recursive cte_categories(id, name, parent_id) as (
      select
        id,
        name,
        parent_id
      from
        categories
      where
        id = $id
      union all
      select
        c.id,
        c.name,
        c.parent_id
      from
        categories c
	    join cte_categories on
		    c.id = cte_categories.parent_id
      )
      select * from cte_categories
      """.as(categoryParser.*)
    }.map(list => list.foldRight[Option[Category]](None){
      case ((id, name, _), parent) => Some(Category(id, name, parent))
    }.get)
  }


  private def withConnection[A](f: Connection => A): Future[A] = Future {
    val connection = jdbcDataSource.getConnection
    try f(connection)
    finally connection.close()
  }
}

object DataSourceDao {

  case class Category(id: String, name: String, parent: Option[Category])
  case class Parameter(id: String, name: String, category: Category)
  case class DataSource(id: String, name: String, parameters: Set[Parameter])
}
