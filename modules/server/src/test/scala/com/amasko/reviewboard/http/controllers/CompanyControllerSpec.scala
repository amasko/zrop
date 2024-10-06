package com.amasko.reviewboard.http.controllers

import com.amasko.reviewboard.http.requests.CreateCompanyRequest
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import sttp.tapir.generic.auto.*
import zio.Scope
import zio.*
import zio.test.*
import zio.json.*
import zio.test.Assertion.*
import sttp.client3.*
import com.amasko.reviewboard.domain.data.Company
import com.amasko.reviewboard.repositories.CompanyRepoMock
import com.amasko.reviewboard.services.CompanyServiceLive
import sttp.tapir.server.ServerEndpoint

object CompanyControllerSpec extends ZIOSpecDefault {

  given MonadError[Task] = new RIOMonadError[Any]
  
  def backend(fn: CompanyController => ServerEndpoint[Any, Task]) =
    for
      companyController <- CompanyController.makeZIO
      backend <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointRunLogic(fn(companyController))
          // .whenEndpoint(companyController.createEndpoint)
          .backend()
      )
    yield backend

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyController")(
      test("should create a company") {
        for
          b <- backend(_.createCompany)
          response <- basicRequest.post(uri"/companies")
            .body(CreateCompanyRequest("Company Name", "nompanyname.com", None, None).toJson)
//            .response(asJson[CreateCompanyRequest])
            .send(b)
          body <- ZIO.from(response.body.map(_.fromJson[Company]))
        yield assertTrue(body == """{"id":1,"name":"Company Name","slug":"company-name","url":"nompanyname.com"}""".fromJson[Company])
      }
    ).provide(
      CompanyServiceLive.layer,
      CompanyRepoMock.layer
    )

}
