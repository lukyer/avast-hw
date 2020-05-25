package com.svacina.hw.grpc

import com.svacina.scanner.Request
import com.svacina.scanner.ScannerServiceGrpcKt.ScannerServiceCoroutineStub
import com.svacina.scanner.Status
import kotlinx.coroutines.flow.Flow

open class GrpcServer(private val stub: ScannerServiceCoroutineStub) {
    open fun scanUrl(requests: Flow<Request>): Flow<Status> {
        return stub.scanUrl(requests)
    }
}