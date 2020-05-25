package com.svacina.hw.router

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.svacina.hw.dto.ScanResponse
import com.svacina.hw.grpc.GrpcServer
import com.svacina.scanner.Request
import com.svacina.scanner.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.util.Assert
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier

private const val POST_REQUEST = """{"urls": ["http://site.com", "http://second.com"]}"""

@SpringBootTest
class ScanRouterTest(@Autowired private val context: ApplicationContext) {

    val client = WebTestClient.bindToApplicationContext(context).build()

    @MockBean
    private lateinit var grpcServer: GrpcServer

    @Test
    fun whenRequestScanWithProblem_thenStatusShouldBe500() {
        prepareGrpcException()
        client.post()
                .uri("/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(POST_REQUEST))
                .exchange()
                .expectStatus()
                .is5xxServerError
    }

    @Test
    fun whenRequestScan_thenStatusShouldBeOk() {
        prepareGrpc()
        client.post()
                .uri("/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(POST_REQUEST))
                .exchange()
                .expectStatus()
                .isOk
    }

    @Test
    fun whenRequestScan_thenResponseShouldBeJsonStreamType() {
        prepareGrpc()
        client.post()
                .uri("/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(POST_REQUEST))
                .exchange()
                .expectHeader()
                .contentType(MediaType.APPLICATION_STREAM_JSON)
    }

    @Test
    fun whenRequestScan_thenResponseShouldBeValid() {
        prepareGrpc()
        val resultFlux = client.post()
                .uri("/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(POST_REQUEST))
                .exchange()
                .returnResult<ScanResponse>()
                .responseBody
        StepVerifier.create(resultFlux)
                .expectNext(ScanResponse("http://site.com", 200, 42))
                .expectNext(ScanResponse("http://second.com", 200, 42))
                .thenCancel()
                .verify()
    }

    private fun prepareGrpc() {
        whenever(grpcServer.scanUrl(any())).then {
            it.getArgument<Flow<Request>>(0).map { request ->
                Status.newBuilder().setCode(200).setTime(42).setUrl(request.url).build()
            }
        }
    }

    private fun prepareGrpcException() {
        whenever(grpcServer.scanUrl(any())).then {
            throw RuntimeException()
        }
    }

    @Test
    fun whenRequestHomepage_thenStatusShouldBeOk() {
        client.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk
    }

    @Test
    fun whenRequestHomepage_thenResponseShouldContainHi() {
        client
                .get()
                .uri("/")
                .exchange()
                .expectBody<String>()
                .consumeWith { Assert.hasText(it.responseBody, "Hi there!") }
    }
}