package com.svacina.hw.handler

import com.svacina.hw.dto.ScanRequest
import com.svacina.hw.dto.ScanResponse
import com.svacina.scanner.Request
import com.svacina.scanner.ScannerServiceGrpcKt
import io.grpc.ManagedChannel
import io.grpc.StatusException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*

@Component
class ScanHandler(private val grpcChannel: ManagedChannel) {

    suspend fun scan(request: ServerRequest): ServerResponse {

        val scanRequest = request.bodyToMono<ScanRequest>()
                .flatMapIterable { it.urls }
                .map { Request.newBuilder().setUrl(it).build() }
                .asFlow()

        val stub = ScannerServiceGrpcKt.ScannerServiceCoroutineStub(grpcChannel)

        try {
            val response = stub.scanUrl(scanRequest).onEach {
                println("Received: \n $it")
            }

            val flow = response.map { ScanResponse(it.url, it.code, it.time) }

            return ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                    .bodyAndAwait(flow)
        } catch (e: StatusException) {
            println("Error: $e")
            return ServerResponse
                    .status(500)
                    .buildAndAwait()
        }

    }

    suspend fun homepage(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().renderAndAwait("homepage")
    }

}