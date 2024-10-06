package com.amasko.reviewboard.repositories

import zio.test.*
import zio.*
import org.testcontainers.containers.PostgreSQLContainer
import com.amasko.reviewboard.domain.data.Company
import com.amasko.reviewboard.repositories.CompanyRepo
import io.getquill.SnakeCase
import org.postgresql.ds.PGSimpleDataSource
import io.getquill.jdbczio.Quill

object CompanyRepoSpec extends ZIOSpecDefault:
  
  private def genString() = scala.util.Random.alphanumeric.take(10).mkString 
  
  private def genCompany(): Company = Company(
    0L,
    genString(),
    genString(),
    genString(),
    None, 
    None)

  def spec = suite("CompanyRepoSpec")(
    test("create company") {
      for
        repo <- ZIO.service[CompanyRepo]
        c <- repo.create(Company(0L, "slug", "name", "url", None, None))
        c1 <- repo.getById(c.id)
      yield assertTrue(c1.contains(c))
    },
    test("get all companies") {
      for
        repo <- ZIO.service[CompanyRepo]
        c <- ZIO.foreach(1 to 10)(_ => repo.create(genCompany()))
        c1 <- repo.getAll
      yield assertTrue(c1.toSet == c.toSet)
    }
  )
    .provide(
      CompanyRepoLive.layer,
      dsLayer
    )

  private def pgContainer() = {
    val container: PostgreSQLContainer[Nothing] = new PostgreSQLContainer("postgres").withInitScript("sql/companies.sql")
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
    ZIO.acquireRelease(ZIO.attempt(pgContainer()))(container => ZIO.attempt(container.stop()).catchAll(e => ZIO.logErrorCause(Cause.fail(e))))
      .map(createDatasource)
  } >>> Quill.Postgres.fromNamingStrategy(io.getquill.SnakeCase)

end CompanyRepoSpec
