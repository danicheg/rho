package org.http4s.rho

import cats.effect.IO
import org.http4s.rho.bits.MethodAliases._
import org.http4s.rho.io._
import org.http4s.{Method, Request}
import org.specs2.mutable.Specification

class CompileRoutesSpec extends Specification {

  def getFoo(implicit c: CompileRoutes[IO, _]) =
    GET / "hello" |>> "GetFoo"

  def putFoo(implicit c: CompileRoutes[IO, _]) =
    PUT / "hello" |>> "PutFoo"

  "CompileService" should {
    "Build a single route" in {
      val c = RoutesBuilder[IO]()
      getFoo(c)

      "GetFoo" === RRunner(c.toRoutes()).checkOk(Request(uri = uri"/hello"))
    }

    "Build multiple routes" in {
      val c = RoutesBuilder[IO]()
      getFoo(c)
      putFoo(c)

      "GetFoo" === RRunner(c.toRoutes()).checkOk(Request(uri = uri"/hello"))
      "PutFoo" === RRunner(c.toRoutes()).checkOk(Request(method = Method.PUT, uri = uri"/hello"))
    }

    "Make routes from a collection of RhoRoutes" in {
      import CompileRoutes.Implicit.compiler
      val routes =
        (GET / "hello" |>> "GetFoo") ::
          (PUT / "hello" |>> "PutFoo") :: Nil

      val srvc = CompileRoutes.foldRoutes[IO](routes)
      "GetFoo" === RRunner(srvc).checkOk(Request(uri = uri"/hello"))
      "PutFoo" === RRunner(srvc).checkOk(Request(method = Method.PUT, uri = uri"/hello"))
    }

    "Concatenate correctly" in {
      val c1 = RoutesBuilder[IO](); getFoo(c1)
      val c2 = RoutesBuilder[IO](); putFoo(c2)

      val srvc = c1.append(c2.routes()).toRoutes()
      "GetFoo" === RRunner(srvc).checkOk(Request(uri = uri"/hello"))
      "PutFoo" === RRunner(srvc).checkOk(Request(method = Method.PUT, uri = uri"/hello"))
    }
  }

}
