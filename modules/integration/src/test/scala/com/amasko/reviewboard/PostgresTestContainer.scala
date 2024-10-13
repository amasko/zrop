package com.amasko.reviewboard

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import zio.{Cause, ZIO, ZLayer}

object PostgresTestContainer:

  private def pgContainer() = {
    val container: PostgreSQLContainer[Nothing] =
      new PostgreSQLContainer("postgres").withInitScript("sql/integration.sql")
    container.start()
    container
  }

  private def createDatasource(container: PostgreSQLContainer[Nothing]): javax.sql.DataSource = {
    val datasource = new PGSimpleDataSource()
    datasource.setUrl(container.getJdbcUrl)
    datasource.setUser(container.getUsername)
    datasource.setPassword(container.getPassword)

    datasource
  }

  val dsLayer: ZLayer[Any, Throwable, Quill.Postgres[SnakeCase]] = ZLayer.scoped {
    ZIO
      .acquireRelease(ZIO.attempt(pgContainer()))(container =>
        ZIO.attempt(container.stop()).catchAll(e => ZIO.logErrorCause(Cause.fail(e)))
      )
      .map(createDatasource)
  } >>> Quill.Postgres.fromNamingStrategy(io.getquill.SnakeCase)
