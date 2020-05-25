package com.svacina.scanner

import akka.actor.ActorSystem
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

object ScannerServer {

  var system: ActorSystem = _

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.defaultApplication()
    system = ActorSystem("Scanner", conf)
    new ScannerServer(system).run()
  }
}

class ScannerServer(system: ActorSystem) {

  def run(): Future[Http.ServerBinding] = {
    implicit val sys: ActorSystem = system
    implicit val mat: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = sys.dispatcher

    val service: HttpRequest => Future[HttpResponse] =
      ScannerServiceHandler(new ScannerServiceImpl(mat))

    val bound = Http().bindAndHandleAsync(
      service,
      interface = "0.0.0.0",
      port = 8980,
      connectionContext = HttpConnectionContext(http2 = Always)
    )

    bound.foreach { binding =>
      println(s"gRPC server bound to: ${binding.localAddress}")
    }

    bound
  }
}