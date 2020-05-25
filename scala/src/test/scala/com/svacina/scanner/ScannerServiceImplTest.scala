package com.svacina.scanner

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{BeforeAndAfterEach, _}

import scala.language.postfixOps

class ScannerServiceImplTest extends AsyncFlatSpec with BeforeAndAfterEach {
  private val port = 9999
  private val hostname = "localhost"
  private val url = s"http://$hostname:$port"

  private val wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))
  private implicit val sys: ActorSystem = ActorSystem.create()
  private val service = new ScannerServiceImpl

  override def beforeEach {
    wireMockServer.start()
    configureHttpServer()
  }

  override def afterEach {
    wireMockServer.stop()
  }

  def configureHttpServer() {
    wireMockServer.stubFor(
      head(urlPathEqualTo("/gen200"))
        .willReturn(aResponse()
          .withStatus(200)))

    wireMockServer.stubFor(
      head(urlPathEqualTo("/gen404"))
        .willReturn(aResponse()
          .withStatus(404)))
  }

  "incoming url" should "send http HEAD request" in {
    val path = "/gen200"
    val request = Request(url + path)
    val response = service.process(request)

    response.map { status =>
      wireMockServer.verify(headRequestedFor(urlPathEqualTo(path)))
      assert(status != null)
    }
  }

  "successful http request" should "propagate correct data" in {
    val path1 = "/gen200"
    val path2 = "/gen404"
    val request1 = Request(url + path1)
    val request2 = Request(url + path2)
    val response1 = service.process(request1)
    val response2 = service.process(request2)

    val responses = for {
      r1 <- response1
      r2 <- response2
    } yield (r1, r2)

    responses.map { case (r1, r2) =>
      assert(r1.code == 200)
      assert(r2.code == 404)
      assert(r1.url == url + path1)
      assert(r2.url == url + path2)
      assert(r1.time > 0)
      assert(r2.time > 0)
    }
  }

  "failed http HEAD request" should "propagate zero status code" in {
    val request = Request("http://42notexistingurl.com")
    val response = service.process(request)

    response.map { status =>
      assert(status.code == 0)
    }
  }

  "grpc call scanUrl" should "stream results" in {
    val path1 = "/gen200"
    val path2 = "/gen404"
    val request1 = Request(url + path1)
    val request2 = Request(url + path2)

    val in = Source(List(request1, request2))
    val response = service.scanUrl(in)

    response.runWith(Sink.collection).map { statuses =>
      assert(statuses.iterator.find(_.url == url + path1).map(_.code).get == 200)
      assert(statuses.iterator.find(_.url == url + path2).map(_.code).get == 404)
    }
  }
}
