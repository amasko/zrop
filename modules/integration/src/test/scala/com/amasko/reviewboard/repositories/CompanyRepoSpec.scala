package com.amasko.reviewboard.repositories

import com.amasko.reviewboard.PostgresTestContainer
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

  private def genCompany(): Company =
    Company(0L, genString(), genString(), genString(), None, None, None, None, Nil)

  def spec = suite("CompanyRepoSpec")(
    test("create company") {
      for
        repo <- ZIO.service[CompanyRepo]
        c    <- repo.create(Company(0L, "slug", "name", "url", None, None, None, None, Nil))
        c1   <- repo.getById(c.id)
      yield assertTrue(c1.contains(c))
    },
    test("get all companies") {
      for
        repo <- ZIO.service[CompanyRepo]
        c    <- ZIO.foreach(1 to 10)(_ => repo.create(genCompany()))
        c1   <- repo.getAll
      yield assertTrue(c1.toSet == c.toSet)
    }
  )
    .provide(
      CompanyRepoLive.layer,
      PostgresTestContainer.dsLayer
    )

end CompanyRepoSpec
