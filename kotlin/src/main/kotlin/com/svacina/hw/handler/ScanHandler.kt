package com.svacina.hw.handler

import com.svacina.hw.dto.ScanRequest
import com.svacina.hw.dto.ScanResponse
import com.svacina.hw.grpc.GrpcServer
import com.svacina.scanner.Request
import io.grpc.StatusException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*

@Component
class ScanHandler(private val grpcServer: GrpcServer) {
    private val logger = KotlinLogging.logger {}

    suspend fun scan(request: ServerRequest): ServerResponse {

        val scanRequest = request.bodyToMono<ScanRequest>()
                .flatMapIterable { it.urls }
                .map { Request.newBuilder().setUrl(it).build() }
                .asFlow()

        try {
            val grpcResponse = grpcServer.scanUrl(scanRequest).onEach {
                logger.debug { "Received: \n $it" }
            }
            val restResponse = grpcResponse.map { ScanResponse(it.url, it.code, it.time) }

            return ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_STREAM_JSON)
                    .bodyAndAwait(restResponse)
        } catch (e: StatusException) {
            logger.error { e }
            return ServerResponse
                    .status(500)
                    .buildAndAwait()
        }

    }

    suspend fun homepage(request: ServerRequest): ServerResponse {
        return ServerResponse.ok().renderAndAwait("homepage")
    }

}