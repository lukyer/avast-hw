package com.svacina.hw

import com.svacina.hw.handler.ScanHandler
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfiguration {

    @Bean
    fun channel(): ManagedChannel = ManagedChannelBuilder
            .forAddress("hw-scala-dev", 8980)
            .usePlaintext()
            .build()
}