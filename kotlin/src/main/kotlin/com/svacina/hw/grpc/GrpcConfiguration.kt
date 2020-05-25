package com.svacina.hw.grpc

import com.svacina.scanner.ScannerServiceGrpcKt.ScannerServiceCoroutineStub
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfiguration {

    @Bean
    fun grpcServer(grpcChannel: ManagedChannel): GrpcServer =
            GrpcServer(ScannerServiceCoroutineStub(grpcChannel))

    @Bean
    fun grpcChannel(): ManagedChannel = ManagedChannelBuilder
            .forAddress("hw-scala-dev", 8980)
            .usePlaintext()
            .build()
}