package tech.responsibly.util

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import org.flywaydb.core.Flyway
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Suite }
import org.testcontainers.containers.PostgreSQLContainer

import javax.sql.DataSource

trait CleanDbPerUnitTest extends BeforeAndAfterAll with BeforeAndAfterEach { self: Suite =>

  private lazy val _container: PostgreSQLContainer[_] = {
    val pg = new PostgreSQLContainer("postgres:13")
    pg.addEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
    pg
  }

  private lazy val _dataSource: DataSource = {
    val config = new HikariConfig()
    config.setDriverClassName("org.postgresql.Driver")
    config.setInitializationFailTimeout(-1)

    val host     = _container.getHost
    val port     = _container.getFirstMappedPort
    val database = "test"

    config.setJdbcUrl(s"jdbc:postgresql://$host:$port/$database?prepareThreshold=0")
    config.setUsername("test")
    config.setPassword("")

    new HikariDataSource(config)
  }

  override protected def beforeAll(): Unit = {
    _container.start()
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    try {
      val hikari: HikariDataSource = jdbcDataSource().asInstanceOf[HikariDataSource]
      hikari.close()
    } finally _container.stop()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    //Apply all migrations
    Flyway.configure().dataSource(jdbcDataSource()).load().migrate()

    val c = jdbcDataSource().getConnection
    try {
      //Truncate all tables in order to remove any data that might have been inserted through the migration scripts
      //in order to not clash with data inserted by tests
      import anorm.SqlStringInterpolation
      SQL"""
      create function truncate_all_tables()
      returns void
      language plpgsql
      as $$$$
      declare
        statements cursor for
        select tablename from pg_tables
        where schemaname = 'public' and tablename != 'flyway_schema_history';
      begin
        for stmt in statements loop
          execute 'truncate table ' || quote_ident(stmt.tablename) || ' cascade;';
        end loop;
      end
      $$$$
      """.execute()(c)
      SQL"select truncate_all_tables()".execute()(c)
    } finally c.close()

    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    // This will DROP the following schemas:
    // public

    // The reason we do this after each test is that undo() is a paid Flyway Teams feature, which we don't have here
    val c = jdbcDataSource().getConnection
    try {
      import anorm.SqlStringInterpolation
      SQL"DROP schema public cascade".execute()(c)
      SQL"CREATE schema public".execute()(c)
    } finally c.close()

    super.afterEach()
  }

  def jdbcDataSource(): DataSource = _dataSource
}
