package com.svacina.scanner

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.Source

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class ScannerServiceImpl(materializer: Materializer) extends ScannerService {
  private val MAX_CONCURRENCY = 4 * Runtime.getRuntime.availableProcessors()

  import materializer.executionContext

  def process(request: Request): Future[Status] = {
    println("Entry process " + Thread.currentThread().getName)

    val promise = Promise[Status]
    implicit val system: ActorSystem = ScannerServer.system

    val start = System.currentTimeMillis()
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = request.url, method = HttpMethods.HEAD))

    responseFuture
      .onComplete {
        case Success(res) => {
          println("Success process " + Thread.currentThread().getName)
          val d = System.currentTimeMillis() - start;
          promise.success(Status(request.url, res.status.intValue(), d.toInt))
          res.discardEntityBytes()
        }
        case Failure(e) => {
          println("Failure process " + e + Thread.currentThread().getName)
          val d = System.currentTimeMillis() - start;
          promise.success(Status(request.url, 0, d.toInt))
        }
      }

    println("Exit process " + Thread.currentThread().getName)
    promise.future
  }


  override def scanUrl(in: Source[Request, NotUsed]): Source[Status, NotUsed] = {
    in.mapAsyncUnordered(MAX_CONCURRENCY)(process)
  }
}
