package tech.responsibly

import anorm.SqlStringInterpolation
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import tech.responsibly.util.CleanDbPerUnitTest

import java.sql.Connection
import scala.concurrent.ExecutionContext

class DataSourceDaoTestSuite extends AnyFunSuite with Matchers with ScalaFutures with CleanDbPerUnitTest {

  private val ec: ExecutionContext = ExecutionContext.global

  private def insertDataSource(id: String, name: String): Unit = {
    implicit val connection: Connection = jdbcDataSource().getConnection
    try SQL"""
      insert into data_sources(id, name)
      values($id, $name)
      """.executeUpdate()
    finally connection.close()
  }
  private def insertCategory(id: String, name: String, parentId: String): Unit = {
    implicit val connection: Connection = jdbcDataSource().getConnection
    try SQL"""
      insert into categories(id, name, parent_id)
      values($id, $name, $parentId)
      """.executeUpdate()
    finally connection.close()
  }
  private def insertParameter(id: String, name: String, categoryId: String): Unit = {
    implicit val connection: Connection = jdbcDataSource().getConnection
    try SQL"""
      insert into parameters(id, name, category_id)
      values($id, $name, $categoryId)
      """.executeUpdate()
    finally connection.close()
  }
  private def insertDsParameter(dsId: String, pId: String): Unit = {
    implicit val connection: Connection = jdbcDataSource().getConnection
    try SQL"""
      insert into data_source_parameters(data_source_id, parameter_id)
      values($dsId, $pId)
      """.executeUpdate()
    finally connection.close()
  }



  test("Get data source by id") {
    val dataSourceId   = "data-source-1"
    val dataSourceName = "Data source 1"
    insertDataSource(dataSourceId, dataSourceName)

    insertCategory("category-2", "Social impact", null)
    insertCategory("category-1", "Diversity and inclusion", "category-2")
    insertCategory("category-3", "Environmental impact", null)

    insertParameter("parameter-1", "Non-discrimination", "category-1")
    insertParameter("parameter-2", "Bio diversity", "category-3")

    insertDsParameter("datasource-1", "parameter-1")
    insertDsParameter("datasource-1", "parameter-2")

    val dao = new DataSourceDao(jdbcDataSource())(ec)

    val dataSourceOption = dao.getById(dataSourceId).futureValue

    dataSourceOption.isDefined shouldBe true
  }
}
